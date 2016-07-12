package uk.elementarysoftware.quickcsv.api;

import java.util.List;

/**
 * CSV Record with header that gives access to all fields from enumaration K.
 * The fields can be accessed using enum values or via CSVRecord API in enum value order.
 * Enum values toString() should match with header column names.
 * 
 * @param <K> - field enumeration
 */
public interface CSVRecordWithHeader<K extends Enum<K>> extends CSVRecord {
    
    public Field getField(K field);
    
    public List<String> getHeader();
}
