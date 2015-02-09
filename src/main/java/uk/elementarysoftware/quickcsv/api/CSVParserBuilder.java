package uk.elementarysoftware.quickcsv.api;

import java.util.Optional;

import uk.elementarysoftware.quickcsv.parser.QuickCSVParser;

public class CSVParserBuilder {
    
    private int bufferSize = 512*1024;
    
    private CSVFileMetadata metadata = CSVFileMetadata.RFC_4180;
    
    public static CSVParserBuilder aParser() {
        return new CSVParserBuilder();
    }
    
    public CSVParserBuilder forTabs() {
        this.metadata = CSVFileMetadata.TABS;
        return this;
    }
    
    public CSVParserBuilder forRfc4180() {
        this.metadata = CSVFileMetadata.RFC_4180;
        return this;
    }
    
    public CSVParserBuilder usingSeparatorWithNoQuotes(char separator) {
        this.metadata = new CSVFileMetadata(separator, Optional.empty());
        return this;
    }
    
    public CSVParserBuilder usingSeparatorWithQuote(char separator, char quote) {
        this.metadata = new CSVFileMetadata(separator, Optional.of(quote));
        return this;
    }
    
    
    public CSVParserBuilder usingBufferSize(int size) {
        this.bufferSize = size;
        return this;
    }  
    
    public CSVParser build() {
        return new QuickCSVParser(bufferSize, metadata);
    }
    
    public static class CSVFileMetadata {
        
        public static CSVFileMetadata RFC_4180 = new CSVFileMetadata(',', Optional.of('"'));
        public static CSVFileMetadata TABS = new CSVFileMetadata('\t', Optional.empty());
        
        public final char separator;
        public final Optional<Character> quote;

        public CSVFileMetadata(char separator, Optional<Character> quote) {
            this.separator = separator;
            this.quote = quote;
        }
    }
}