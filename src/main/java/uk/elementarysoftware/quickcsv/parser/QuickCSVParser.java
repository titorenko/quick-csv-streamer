package uk.elementarysoftware.quickcsv.parser;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import uk.elementarysoftware.quickcsv.api.ByteArraySource;
import uk.elementarysoftware.quickcsv.api.ByteArraySource.ByteArrayChunk;
import uk.elementarysoftware.quickcsv.api.CSVParser;
import uk.elementarysoftware.quickcsv.api.CSVParserBuilder.CSVFileMetadata;
import uk.elementarysoftware.quickcsv.functional.Pair;
import uk.elementarysoftware.quickcsv.api.CSVRecord;
import uk.elementarysoftware.quickcsv.api.CSVRecordWithHeader;
import uk.elementarysoftware.quickcsv.api.Field;

public class QuickCSVParser<T, K extends Enum<K>> implements CSVParser<T> {

    private final CSVFileMetadata metadata;
    private final int bufferSize;
    private final Function<CSVRecord, T> mapper;
    private final Optional<FieldSubsetView<K>> fieldSubsetView;
    private final Charset charset;

    public QuickCSVParser(int bufferSize, CSVFileMetadata metadata, Function<CSVRecordWithHeader<K>, T> mapper, 
            FieldSubsetView<K> fieldSubsetView, Charset charset) {
        this.metadata = metadata;
        this.bufferSize = bufferSize;
        this.mapper = cast(mapper);
        this.fieldSubsetView = Optional.of(fieldSubsetView);
        this.charset = charset;
    }
    
    public QuickCSVParser(int bufferSize, CSVFileMetadata metadata, Function<CSVRecord, T> mapper, Charset charset) {
        this.metadata = metadata;
        this.bufferSize = bufferSize;
        this.mapper = mapper;
        this.fieldSubsetView = Optional.empty();
        this.charset = charset;
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
            ByteSlice nextSlice = nextSlice();
            if (!nextSlice.hasMoreData()) return false;
            this.sequentialSplitterator = sliceSpliterator(nextSlice);
            return tryAdvance(action);
        }

        @Override
        public Spliterator<T> trySplit() {
            if (isEndReached) return null;
            ByteSlice nextSlice = nextSlice();
            if (!nextSlice.hasMoreData()) return null;
            return sliceSpliterator(nextSlice);
        }
        
        private ByteSlice nextSlice() {
            ByteSlice bareSlice = nextBareSlice();
            bareSlice.incrementUse();
            if (isEndReached) {
                return ByteSlice.join(prefix, bareSlice);
            } else {
                Pair<ByteSlice, ByteSlice> sliced = bareSlice.splitOnLastLineEnd();
                ByteSlice result = ByteSlice.join(prefix, sliced.first);
                this.prefix = sliced.second;
                bareSlice.incrementUse();
                return result;
            }
        }

        private ByteSlice nextBareSlice() {
            try {
                ByteArrayChunk it = bas.getNext();
                this.isEndReached = it.isLast();
                ByteSlice slice = ByteSlice.wrap(it, charset);
                if (fieldSubsetView.isPresent()) fieldSubsetView.get().onSlice(slice, metadata);
                return slice;
            } catch (RuntimeException e) {
                throw e;
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
            if (!slice.hasMoreData()) {
                slice.decremenentUse();
                return false;
            }
            advance(action);
            return true;
        }

        protected void advance(Consumer<? super T> action) {
            T t = mapper.apply(this);
            action.accept(t);
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
        private final ByteArrayField[] fieldTemplates; 

        public LensingByteSliceSpliterator(ByteSlice slice) {
            super(slice);
            this.view = fieldSubsetView.get();
            this.fieldTemplates = new ByteArrayField[view.getFieldSubsetSize()];
            for (int i = 0; i < fieldTemplates.length; i++) {
                fieldTemplates[i] = new ByteArrayField(null, -1, -1, charset);
            }
        }
        
        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            if (!slice.hasMoreData()) {
                slice.decremenentUse();
                return false;
            }
            parseFields();
            super.advance(action);
            return true;
        }

        private void parseFields() {
            int[] skipSchedule = view.getFieldSkipSchedule();
            for (int i = 0; i < skipSchedule.length; i++) {
                skipFields(skipSchedule[i]);
                ByteArrayField field = super.getNextField();//TODO: init into template directly
                fieldTemplates[i].initFrom(field);
            }
        }

        @Override
        public Field getField(K fieldName) {
            return fieldTemplates[view.indexOfInSourceView(fieldName.ordinal())];
        }

        @Override
        public List<String> getHeader() {
            return view.getHeader();
        }
    }
}