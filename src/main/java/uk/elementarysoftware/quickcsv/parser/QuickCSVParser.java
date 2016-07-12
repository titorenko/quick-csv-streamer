package uk.elementarysoftware.quickcsv.parser;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import uk.elementarysoftware.quickcsv.api.ByteArraySource;
import uk.elementarysoftware.quickcsv.api.ByteArraySource.ByteArrayItem;
import uk.elementarysoftware.quickcsv.api.CSVParser;
import uk.elementarysoftware.quickcsv.api.CSVParserBuilder.CSVFileMetadata;
import uk.elementarysoftware.quickcsv.api.CSVRecord;
import uk.elementarysoftware.quickcsv.api.CSVRecordWithHeader;
import uk.elementarysoftware.quickcsv.api.ExceptionHandler;
import uk.elementarysoftware.quickcsv.api.Field;
import uk.elementarysoftware.quickcsv.tuples.Pair;

public class QuickCSVParser<T, K extends Enum<K>> implements CSVParser<T> {

    private final CSVFileMetadata metadata;
    private final int bufferSize;
	private final Function<CSVRecord, T> mapper;
	private final ExceptionHandler mappingExceptionHandler;
    private final ExceptionHandler consumerExceptionHandler;
    private final Optional<FieldSubsetView<K>> fieldSubsetView;

    public QuickCSVParser(int bufferSize, CSVFileMetadata metadata, Function<CSVRecordWithHeader<K>, T> mapper, 
    		FieldSubsetView<K> fieldSubsetView, ExceptionHandler mappingExceptionHandler, ExceptionHandler consumerExceptionHandler) {
	    this.metadata = metadata;
	    this.bufferSize = bufferSize;
	    this.mapper = cast(mapper);
	    this.fieldSubsetView = Optional.of(fieldSubsetView);
	    this.mappingExceptionHandler = mappingExceptionHandler;
	    this.consumerExceptionHandler = consumerExceptionHandler;
	}
    
    public QuickCSVParser(int bufferSize, CSVFileMetadata metadata, Function<CSVRecord, T> mapper, 
    		ExceptionHandler mappingExceptionHandler, ExceptionHandler consumerExceptionHandler) {
	    this.metadata = metadata;
	    this.bufferSize = bufferSize;
	    this.mapper = mapper;
	    this.fieldSubsetView = Optional.empty();
	    this.mappingExceptionHandler = mappingExceptionHandler;
	    this.consumerExceptionHandler = consumerExceptionHandler;
	}
    
	@SuppressWarnings("unchecked")
    private static <T, K extends Enum<K>> Function<CSVRecord, T> cast(Function<CSVRecordWithHeader<K>, T> f) {
        return r -> f.apply((CSVRecordWithHeader<K>) r);
    }

    
    @Override
    public Stream<T> parse(InputStream is) {
        BufferPool pool = new BufferPool(bufferSize);
        return parse(new InputStreamToByteArraySourceAdapter(is, pool));
    }

    @Override
    public Stream<T> parse(ByteArraySource bas) {
        return StreamSupport.stream(new SplittingSpliterator(bas), true);
    }

    class SplittingSpliterator implements Spliterator<T> {
        
        private final ByteArraySource bas;
        
        private ByteSlice prefix = ByteSlice.empty(); 
        private boolean isEndReached = false;

        private Spliterator<T> sequentialSplitterator = Spliterators.emptySpliterator();

        SplittingSpliterator(ByteArraySource bas) {
        	this.bas = bas;
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) { //usually only called in sequential mode
            boolean advanced = sequentialSplitterator.tryAdvance(action);
            if (advanced) return true;
            if (isEndReached) return false;

            ByteSlice bareSlice = nextSlice();
            ByteSlice nextSlice;
            if (isEndReached) {
                nextSlice = ByteSlice.join(prefix, bareSlice);
            } else {
                Pair<ByteSlice, ByteSlice> sliced = bareSlice.splitOnLastLineEnd();
                nextSlice = ByteSlice.join(prefix, sliced.first);
                this.prefix = sliced.second;
            }
            if (!nextSlice.hasMoreData()) return false;
            this.sequentialSplitterator = sliceSpliterator(nextSlice);
            return tryAdvance(action);
        }

        @Override
        public Spliterator<T> trySplit() {
            if (isEndReached) return null;
            ByteSlice bareSlice = nextSlice();
            ByteSlice nextSlice;
            if (isEndReached) {
                nextSlice = ByteSlice.join(prefix, bareSlice);
            } else {
                Pair<ByteSlice, ByteSlice> sliced = bareSlice.splitOnLastLineEnd();
                nextSlice = ByteSlice.join(prefix, sliced.first);
                this.prefix = sliced.second;
            }
            if (!nextSlice.hasMoreData()) return null;
            return sliceSpliterator(nextSlice);
        }

        private ByteSlice nextSlice() {
            try {
                ByteArrayItem it = bas.getNext();
                this.isEndReached = it.isLast();
                ByteSlice slice = ByteSlice.wrap(it.getData(), it.getLength());
                if (fieldSubsetView.isPresent()) fieldSubsetView.get().onSlice(slice, metadata);
                return slice;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public long estimateSize() {
           return Long.MAX_VALUE;
        }

        @Override
        public int characteristics() {
            return ORDERED | NONNULL | IMMUTABLE;
        }
    }
    
    Spliterator<T> sliceSpliterator(ByteSlice slice) {
        return fieldSubsetView.isPresent() ? new LensingByteSliceSpliterator(slice) : new ByteSliceSpliterator(slice);
    }
    
    class ByteSliceSpliterator implements Spliterator<T>, CSVRecord {

        protected final ByteSlice slice;

        ByteSliceSpliterator(ByteSlice slice) {
            this.slice = slice;//incoming slice should have no broken lines
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            if (!slice.hasMoreData())
                return false;
            advance(action);
            return true;
        }

        protected void advance(Consumer<? super T> action) {
            try {
            T t = mapper.apply(this);
                try {
            action.accept(t);
                } catch (RuntimeException e) {
                    consumerExceptionHandler.onException(e, this.slice.currentLine());
                }
            } catch (RuntimeException e) {
                mappingExceptionHandler.onException(e, this.slice.currentLine());
            }
            slice.nextLine();
        }

        @Override
        public Spliterator<T> trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return slice.size();
        }

        @Override
        public int characteristics() {
            return ORDERED | NONNULL | IMMUTABLE;
        }

        @Override
        public void skipField() {
            slice.skipField(metadata);
        }

        @Override
        public void skipFields(int nFields) {
            for (int i = 0; i < nFields; i++) {
                skipField();
            }
        }

        @Override
        public ByteArrayField getNextField() {
            return slice.getNextField(metadata);
        }
    }
    
    class LensingByteSliceSpliterator extends ByteSliceSpliterator implements CSVRecordWithHeader<K> {

        private final FieldSubsetView<K> view;
        private int viewFieldIndex;
        private final ByteArrayField[] fieldTemplates; 

        public LensingByteSliceSpliterator(ByteSlice slice) {
            super(slice);
            this.view = fieldSubsetView.get();
            this.fieldTemplates = new ByteArrayField[view.getFieldIndexes().length];
            for (int i = 0; i < fieldTemplates.length; i++) {
                fieldTemplates[i] = new ByteArrayField(null, -1, -1);
            }
        }
        
        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            if (!slice.hasMoreData())
                return false;
            this.viewFieldIndex = 0;
            parseFields();
            super.advance(action);
            return true;
        }

        private void parseFields() {
            int[] fieldIndexes = view.getFieldIndexes();
            int lastFieldIndex = -1;
            for (int i = 0; i < fieldIndexes.length; i++) {
                int idx = fieldIndexes[i];
                int nSkip = idx - lastFieldIndex - 1;
                skipSourceFields(nSkip);
                ByteArrayField field = super.getNextField();
                fieldTemplates[i].initFrom(field);
                lastFieldIndex = idx;
            }
        }
        
        @Override
        public void skipField() {
            viewFieldIndex++;
        }
        
        @Override
        public void skipFields(int nFields) {
            viewFieldIndex += nFields;
        }
        
        private void skipSourceFields(int nFields) {
            for (int i = 0; i < nFields; i++) {
                super.skipField();
            }
        }
        
        @Override
        public ByteArrayField getNextField() {
            return fieldTemplates[view.indexOfInSourceView(viewFieldIndex++)];
        }

        @Override
        public Field getField(K fieldName) {
            try {
                return fieldTemplates[view.indexOfInSourceView(fieldName)];
            } catch (RuntimeException e) {
                throw new RuntimeException("Unknown field: "+fieldName, e);
            }
        }

		@Override
		public List<String> getHeader() {
			return view.getHeader();
		}
    }
}