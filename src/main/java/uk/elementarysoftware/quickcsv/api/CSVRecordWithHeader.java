package uk.elementarysoftware.quickcsv.api;

import java.util.List;

/**
 * CSV Record with header that gives access to all fields from enumeration K.
 * The fields can be accessed by name using enum values.
 * Enum values toString() should match with header column names.
 * 
 * @param <K> - field enumeration
 */
public interface CSVRecordWithHeader<K extends Enum<K>> {
    
    public Field getField(K field);
    
    public List<String> getHeader();
}
