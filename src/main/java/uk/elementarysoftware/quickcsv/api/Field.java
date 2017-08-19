package uk.elementarysoftware.quickcsv.api;

import java.nio.ByteBuffer;

/**
 * Interface to access underlying raw data as particular type.
 *
 * Usually underlying the field is some kind of byte large array and the field maintains view onto this array.
 * Underlying array can be mutated during parsing and the field object itself can be re-used, therefore clients
 * should not maintain references to Field instances, instead client is expected to map field to it's own data
 * structure and the no longer use it.
 * 
 * Methods that return primitive types will throw NPE if underlying field is empty. This should be tested with isEmpty() 
 * call where needed or boxed methods should be used.
 */
public interface Field {

    public ByteBuffer raw();

    public String asString();
    
    public double asDouble();
    public byte asByte();
    public char asChar();
    public short asShort();
    public int asInt();
    public long asLong();
    
    public Integer asBoxedInt();
    public Double asBoxedDouble();

    public boolean isEmpty();

    public Field clone();
}
