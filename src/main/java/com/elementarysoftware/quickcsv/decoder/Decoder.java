package com.elementarysoftware.quickcsv.decoder;

import com.elementarysoftware.quickcsv.decoder.doubles.DoubleParserFactory;

public class Decoder {
	
	private com.elementarysoftware.quickcsv.decoder.doubles.DoubleParser doubleParser;
	
	public Decoder() {
		doubleParser = DoubleParserFactory.getParser();
	}
	
	public String decodeToString(byte[] buffer, int offset, int length) {
	    return new String(buffer, offset, length);
	}

	public double decodeToDouble(byte[] buffer, int offset, int length) {
	    if (length == 0) return 0.0;
	    return doubleParser.parse(buffer, offset, length);
	}
	
	public int decodeToInt(byte[] buffer, int offset, int length) {
	    if (length == 0) return 0;
        return Integer.parseInt(decodeToString(buffer, offset, length));//TODO: optimize me
    }

    public long decodeToLong(byte[] buffer, int offset, int length) {
        if (length == 0) return 0L;
        return Long.parseLong(decodeToString(buffer, offset, length));//TODO: optimize me
    }
}