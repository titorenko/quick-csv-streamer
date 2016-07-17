package uk.elementarysoftware.quickcsv.decoder.doubles;


public interface DoubleParser {
    public double parse(byte[] in, int startIndex, int length);

    default public double parse(String s) {
        return parse(s.getBytes(), 0, s.length());
    };
}
