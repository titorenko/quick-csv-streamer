package uk.elementarysoftware.quickcsv.api;


/**
 * Interface to access parsed CSV data in efficient manner. 
 * Fields are parsed in order they appear in the CSV source.
 */
public interface CSVRecord {
    public void skipField();
    public void skipFields(int nFields);
    
    public Field getNextField();
}