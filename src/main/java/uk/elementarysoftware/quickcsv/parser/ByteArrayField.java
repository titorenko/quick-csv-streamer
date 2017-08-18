package uk.elementarysoftware.quickcsv.parser;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import uk.elementarysoftware.quickcsv.api.Field;
import uk.elementarysoftware.quickcsv.decoder.Decoder;

public class ByteArrayField implements Field {
    
	public static final ByteArrayField EMPTY = new ByteArrayField(new byte[0], 0, 0, null);
	
    private final Decoder decoder;

    private byte[] buffer;
    private int start;
    private int end;
    private Character quote; //if not null indicates that value was actually quoted

    public ByteArrayField(byte[] buffer, int startIndex, int endIndex, Charset charset) {
        this(buffer, startIndex, endIndex, charset, null);
    }

    public ByteArrayField(byte[] buffer, int startIndex, int endIndex, Charset charset, Character quote) {
        this.buffer = buffer;
        this.start = startIndex;
        this.end = endIndex;
        this.quote = quote;
        this.decoder = new Decoder(charset);
    }

    @Override
    public ByteBuffer raw() {
        return ByteBuffer.wrap(buffer, start, end - start);
    }

    @Override
    public String asString() {
    	String result = decoder.decodeToString(buffer, start, end - start);
        if (quote != null && result.indexOf(quote) >= 0) {
        	//TODO: optimise and add more flexible escape character
        	//flag indicating if an escaped quote was seen can be passed from the parser itself as state
            return result.replace(new StringBuffer().append(quote).append(quote), new StringBuffer().append(quote)); 
        } else {
            return result;
        }
    }

    @Override
    public double asDouble() {
        return decoder.decodeToDouble(buffer, start, end - start);
    }

    @Override
    public byte asByte() {
        return (byte) asInt();
    }

    @Override
    public char asChar() {
        return (char) asInt();
    }

    @Override
    public short asShort() {
        return (short) asInt();
    }

    @Override
    public int asInt() {
        return decoder.decodeToInt(buffer, start, end - start);
    }

    @Override
    public long asLong() {
        return decoder.decodeToLong(buffer, start, end - start);
    }

    void modifyBounds(int start, int end) { //re-use object to reduce GC overhead
        this.start = start;
        this.end = end;
        this.quote = null;
    }
    
    void modifyBounds(int start, int end, Character quote) {
        this.start = start;
        this.end = end;
        this.quote = quote;        
    }
    
    public void initFrom(ByteArrayField other) {
        this.buffer = other.buffer;
        this.start = other.start;
        this.end = other.end;
        this.quote = other.quote;
    }
    
    @Override
    public Field clone() {
        return new ByteArrayField(buffer, start, end, decoder.getCharset(), quote);
    }

    @Override
    public boolean isEmpty() {
        return start >= end;
    }

	@Override
	public Double asDoubleWrapper() {
		return isEmpty() ? null : asDouble();
	}

	@Override
	public Integer asInteger() {
		return isEmpty() ? null : asInt();
	}
}