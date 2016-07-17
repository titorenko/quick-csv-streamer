package uk.elementarysoftware.quickcsv.decoder.ints;

public interface LongParser {
    public long parse(byte[] in, int startIndex, int length);

    default public long parse(String s) {
        return parse(s.getBytes(), 0, s.length());
    };
}