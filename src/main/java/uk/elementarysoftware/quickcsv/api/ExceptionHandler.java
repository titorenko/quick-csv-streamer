package uk.elementarysoftware.quickcsv.api;

public interface ExceptionHandler {
    
    /**
     * Handle exception raised during CSV parsing
     * @param ex - exception 
     * @param csvDataRow - CSV row that was processed during error
     */
    public void onException(RuntimeException ex, String csvDataRow);

}
