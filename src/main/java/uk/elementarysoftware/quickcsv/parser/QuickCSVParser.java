package uk.elementarysoftware.quickcsv.parser;

import java.io.InputStream;
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
import uk.elementarysoftware.quickcsv.api.Field;
import uk.elementarysoftware.quickcsv.tuples.Pair;

public class QuickCSVParser<T> implements CSVParser<T> {

    private final CSVFileMetadata metadata;
    private final int bufferSize;
	private final Function<CSVRecord, T> mapper;

    public QuickCSVParser(int bufferSize, CSVFileMetadata metadata, Function<CSVRecord, T> mapper) {
	    this.metadata = metadata;
	    this.bufferSize = bufferSize;
	    this.mapper = mapper;
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
            this.sequentialSplitterator = new ByteSliceSpliterator(nextSlice);
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
            return new ByteSliceSpliterator(nextSlice);
        }

        private ByteSlice nextSlice() {
            try {
                ByteArrayItem it = bas.getNext();
                this.isEndReached = it.isLast();
                return ByteSlice.wrap(it.getData(), it.getLength());
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
    
    class ByteSliceSpliterator implements Spliterator<T>, CSVRecord {

        private ByteSlice slice;

        public ByteSliceSpliterator(ByteSlice slice) {
            this.slice = slice;//incoming slice should have no broken lines
        }

        @Override
        public boolean tryAdvance(Consumer<? super T> action) {
            if (!slice.hasMoreData())
                return false;
            T t = mapper.apply(this);
            action.accept(t);
            slice.nextLine();
            return true;
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
            if (metadata.quote.isPresent()) 
                slice.skipUntil(metadata.separator, metadata.quote.get());
            else
                slice.skipUntil(metadata.separator);
        }

        @Override
        public void skipFields(int nFields) {
            for (int i = 0; i < nFields; i++) {
                skipField();
            }
        }

        @Override
        public Field getNextField() {
            if (metadata.quote.isPresent()) 
                return slice.nextField(metadata.separator, metadata.quote.get());
            else
                return slice.nextField(metadata.separator);
        }
    }
}