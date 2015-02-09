package com.elementarysoftware.quickcsv.api;

import java.nio.ByteBuffer;

public interface Field extends Cloneable {
    public ByteBuffer raw();
    
    public String asString(); //TODO: add charset support

    public double asDouble();
    public byte asByte();
    public char asChar();
    public short asShort();
    public int asInt();
    public long asLong();
    
    public boolean isEmpty();
    
    public Field clone();
}