package uk.elementarysoftware.quickcsv.decoder;

import java.nio.charset.Charset;

import uk.elementarysoftware.quickcsv.decoder.ints.IntParser;
import uk.elementarysoftware.quickcsv.decoder.ints.LongParser;

public class Decoder {
    
    private final uk.elementarysoftware.quickcsv.decoder.doubles.DoubleParser doubleParser;
    private final Charset charset;
    private final IntParser intParser;
    private final LongParser longParser;
    
    public Decoder(Charset charset) {
        this.charset = charset;
        ParserFactory parserFactory = new ParserFactory();
        this.doubleParser = parserFactory.getDoubleParser();
        this.intParser = parserFactory.getIntParser();
        this.longParser = parserFactory.getLongParser();
    }
    
    public String decodeToString(byte[] buffer, int offset, int length) {
        return new String(buffer, offset, length, charset);
    }
    
    public double decodeToDouble(byte[] buffer, int offset, int length) {
        if (length == 0) return 0.0;
        return doubleParser.parse(buffer, offset, length);
    }
    
    public int decodeToInt(byte[] buffer, int offset, int length) {
        if (length == 0) return 0;
        return intParser.parse(buffer, offset, length);
    }

    public long decodeToLong(byte[] buffer, int offset, int length) {
        if (length == 0) return 0L;
        return longParser.parse(buffer, offset, length);
    }

    public Charset getCharset() {
        return charset;
    }
}