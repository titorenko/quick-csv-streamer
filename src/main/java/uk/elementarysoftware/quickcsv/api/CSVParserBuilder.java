package uk.elementarysoftware.quickcsv.api;

import java.util.Optional;
import java.util.function.Function;

import uk.elementarysoftware.quickcsv.parser.QuickCSVParser;

public class CSVParserBuilder<T> {
    
    private int bufferSize = 512*1024;
    
    private CSVFileMetadata metadata = CSVFileMetadata.RFC_4180;

	private final Function<CSVRecord, T> mapper;
    
    private CSVParserBuilder(Function<CSVRecord, T> mapper) {
    	this.mapper = mapper;
	}

	public static <T> CSVParserBuilder<T> aParser(Function<CSVRecord, T> mapper) {
        return new CSVParserBuilder<T>(mapper);
    }
    
    public CSVParserBuilder<T> forTabs() {
        this.metadata = CSVFileMetadata.TABS;
        return this;
    }
    
    public CSVParserBuilder<T> forRfc4180() {
        this.metadata = CSVFileMetadata.RFC_4180;
        return this;
    }
    
    public CSVParserBuilder<T> usingSeparatorWithNoQuotes(char separator) {
        this.metadata = new CSVFileMetadata(separator, Optional.empty());
        return this;
    }
    
    public CSVParserBuilder<T> usingSeparatorWithQuote(char separator, char quote) {
        this.metadata = new CSVFileMetadata(separator, Optional.of(quote));
        return this;
    }
    
    
    public CSVParserBuilder<T> usingBufferSize(int size) {
        this.bufferSize = size;
        return this;
    }
    
    
    public CSVParser<T> build() {
        return new QuickCSVParser<T>(bufferSize, metadata, mapper);
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