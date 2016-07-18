package uk.elementarysoftware.quickcsv.api;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Abstract source of byte arrays to allow parsing of synchronous or asynchronous streams. 
 */
public interface ByteArraySource {

    ByteArrayChunk getNext() throws Exception;
    
    public abstract static class ReusableChunk {

        private final Runnable onFree;
        private final AtomicInteger usageCount = new AtomicInteger(0);
        
        /**
        * @param onFree - callback that will be called when usage count reaches zero
        */
        protected ReusableChunk(Runnable onFree) {
            this.onFree = onFree;
        }
        
        public void incrementUseCount() {
            usageCount.incrementAndGet();
        }
        
        public void decrementUseCount() {
            int value = usageCount.decrementAndGet();
            if (value <= 0) onFree.run();
        }
    }
    
    public static class ByteArrayChunk extends ReusableChunk {
        public static final ByteArrayChunk EMPTY = new ByteArrayChunk(new byte[0], 0, false, (b) -> {});
        
        private final byte[] data;
        private final int length;
        private final boolean isLast;

        /**
         * @param data - underlying content
         * @param length - content length
         * @param isLast - is this chunk of is last
         * @param onFree - callback that will be called when data from this chunk has been fully consumed.
         */
        public ByteArrayChunk(byte[] data, int length, boolean isLast, Consumer<byte[]> onFree) {
            super(() -> onFree.accept(data));
            this.data = data;
            this.length = length;
            this.isLast = isLast;
        }

        public byte[] getData() {
            return data;
        }

        public int getLength() {
            return length;
        }

        public boolean isLast() {
            return isLast;
        }
    }
}