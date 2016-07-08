package uk.elementarysoftware.quickcsv.api;

/**
 * Abstract source of byte arrays to allow parsing of asynchronous streams
 */
public interface ByteArraySource {

    ByteArrayItem getNext() throws Exception;

    public static class ByteArrayItem {
        private final byte[] data;
        private final int length;
        private final boolean isLast;

        public ByteArrayItem(byte[] data, int length, boolean isLast) {
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
