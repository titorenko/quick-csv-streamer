package com.elementarysoftware.quickcsv.api;


public interface CSVRecord {
    public void skipField();
    public void skipFields(int nFields);
    
    public Field getNextField();
}