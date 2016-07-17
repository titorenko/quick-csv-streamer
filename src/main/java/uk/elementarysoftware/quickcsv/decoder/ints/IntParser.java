package uk.elementarysoftware.quickcsv.decoder.ints;

public interface IntParser {
    public int parse(byte[] in, int startIndex, int length);

    default public int parse(String s) {
        return parse(s.getBytes(), 0, s.length());
    };
}
