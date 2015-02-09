package uk.elementarysoftware.quickcsv.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import uk.elementarysoftware.quickcsv.api.CSVParser;
import uk.elementarysoftware.quickcsv.api.CSVRecord;
import uk.elementarysoftware.quickcsv.api.Field;
import uk.elementarysoftware.quickcsv.api.CSVParserBuilder.CSVFileMetadata;
import uk.elementarysoftware.quickcsv.tuples.Pair;
import uk.elementarysoftware.quickcsv.utils.IOUtils;

public class QuickCSVParser implements CSVParser {

    private final CSVFileMetadata metadata;
    private final int bufferSize;

    public QuickCSVParser(int bufferSize, CSVFileMetadata metadata) {
	    this.metadata = metadata;
	    this.bufferSize = bufferSize;
	}

    @Override
    public Stream<CSVRecord> parse(InputStream is) {
        return StreamSupport.stream(new SplittingSpliterator(is), true);
    }

    class SplittingSpliterator implements Spliterator<CSVRecord> {
        
        private final InputStream is;
        private final BufferPool pool;
        
        private ByteSlice prefix = ByteSlice.empty(); 
        private boolean isEndReached = false;

        public SplittingSpliterator(InputStream is) {
            this.is = is;
            this.pool = new BufferPool(bufferSize);
        }
        
        private Spliterator<CSVRecord> sequentialSplitterator = Spliterators.emptySpliterator();

        @Override
        public boolean tryAdvance(Consumer<? super CSVRecord> action) {
            boolean advanced = sequentialSplitterator.tryAdvance(action);
            if (advanced) return true;
            if (isEndReached) return false;

            ByteSlice slice = nextSlice();
            if (isEndReached) {
                this.sequentialSplitterator = new ByteSliceSpliterator(ByteSlice.join(prefix, slice));
            } else {
                Pair<ByteSlice, ByteSlice> sliced = slice.splitOnLastLineEnd();
                this.sequentialSplitterator = new ByteSliceSpliterator(ByteSlice.join(prefix, sliced.first));
                this.prefix = sliced.second;
            }
            return tryAdvance(action);
        }

        @Override
        public Spliterator<CSVRecord> trySplit() {
            if (isEndReached) return null;
            ByteSlice slice = nextSlice();
            if (isEndReached) return new ByteSliceSpliterator(ByteSlice.join(prefix, slice));
            Pair<ByteSlice, ByteSlice> sliced = slice.splitOnLastLineEnd();
            Spliterator<CSVRecord> result = new ByteSliceSpliterator(ByteSlice.join(prefix, sliced.first));
            this.prefix = sliced.second;
            return result;
        }

        private ByteSlice nextSlice() {
            try {
                byte[] buffer = pool.getBuffer();
                int read = is.read(buffer);
                this.isEndReached = read < buffer.length;
                if (isEndReached) IOUtils.closeQuietly(is);
                return ByteSlice.wrap(buffer, read);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public long estimateSize() {
           return Long.MAX_VALUE;
        }

        @Override
        public int characteristics() {
            return ORDERED & NONNULL & IMMUTABLE;
        }
    }
    
    class ByteSliceSpliterator implements Spliterator<CSVRecord>, CSVRecord {

        private ByteSlice slice;

        public ByteSliceSpliterator(ByteSlice slice) {
            this.slice = slice;//incoming slice should have no broken lines
        }

        @Override
        public boolean tryAdvance(Consumer<? super CSVRecord> action) {
            action.accept(this);
            slice.nextLine();
            return slice.hasMoreData();
        }

        @Override
        public Spliterator<CSVRecord> trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return slice.size();
        }

        @Override
        public int characteristics() {
            return ORDERED & NONNULL & IMMUTABLE;
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