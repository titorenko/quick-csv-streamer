package uk.elementarysoftware.quickcsv.parser;

import java.nio.ByteBuffer;

import uk.elementarysoftware.quickcsv.api.Field;
import uk.elementarysoftware.quickcsv.decoder.Decoder;

public class ByteArrayField implements Field {
    
    private final Decoder decoder = new Decoder();

    private byte[] buffer;
    private int start;
    private int end;
    private Character quote;

    public ByteArrayField(byte[] buffer, int startIndex, int endIndex) {
        this(buffer, startIndex, endIndex, null);
    }

    public ByteArrayField(byte[] buffer, int startIndex, int endIndex, Character quote) {
        this.buffer = buffer;
        this.start = startIndex;
        this.end = endIndex;
        this.quote = quote;
    }

    @Override
    public ByteBuffer raw() {
        return ByteBuffer.wrap(buffer, start, end - start);
    }

    @Override
    public String asString() {
        String result = decoder.decodeToString(buffer, start, end - start);
        if (quote != null) {
            return result.replace(new StringBuffer().append(quote).append(quote), new StringBuffer().append(quote)); //TODO: optimize
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
        this.start = other.start;
        this.end = other.end;
        this.buffer = other.buffer;
        this.quote = other.quote;
    }
    
    @Override
    public Field clone() {
        return new ByteArrayField(buffer, start, end, quote);
    }

    @Override
    public boolean isEmpty() {
        return start >= end;
    }
}