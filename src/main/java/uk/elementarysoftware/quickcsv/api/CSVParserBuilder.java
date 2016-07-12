package uk.elementarysoftware.quickcsv.api;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import uk.elementarysoftware.quickcsv.parser.FieldSubsetView;
import uk.elementarysoftware.quickcsv.parser.QuickCSVParser;

public class CSVParserBuilder<T, K extends Enum<K>> {
    
    private int bufferSize = 512*1024;
    
    private CSVFileMetadata metadata = CSVFileMetadata.RFC_4180;

	private Function<CSVRecord, T> recordMapper;

	private Function<CSVRecordWithHeader<K>, T> recordWithHeaderMapper;
	private FieldSubsetView<K> subsetView = null;
	
	private ExceptionHandler mappingExceptionHandler  = (ex, row) -> {throw new RuntimeException("Failed to parse: "+row, ex);};

    private ExceptionHandler consumerExceptionHandler = (ex, row) -> {throw ex;};
    
    private CSVParserBuilder() {
	}

    /**
     * Create new parser using supplied mapping function. 
     * 
     * Mapping function can not store reference to CSVRecord object,  it needs to be a pure function that create new instance of T. 
     * CSVRecord could be mutated by the parser when next record is processed.
     * 
     * @param mapper - mapping function from CSVRecord to T
     * @return this parser builder
     */
	public static <T, K extends Enum<K>> CSVParserBuilder<T, K> aParser(Function<CSVRecord, T> mapper) {
        CSVParserBuilder<T, K> builder = new CSVParserBuilder<T, K>();
        builder.recordMapper = mapper;
        return builder;
    }
	
	public static <T, K extends Enum<K>> CSVParserBuilder<T, K> aParser(Function<CSVRecordWithHeader<K>, T> mapper, Class<K> fields) {
        CSVParserBuilder<T, K> builder = new CSVParserBuilder<T, K>();
        builder.recordWithHeaderMapper = mapper;
        builder.subsetView = FieldSubsetView.forSourceSuppliedHeader(fields);
        return builder;
    }
	
	public CSVParserBuilder<T, K> usingExplicitHeader(String... header) {
		Objects.requireNonNull(subsetView);
		this.subsetView = FieldSubsetView.forExplicitHeader(subsetView.getFieldSubset(), header);
		return this;
	}
    
    /**
	 * Use tabs as separator and no quoting
	 * @return this parser builder
	 */
    public CSVParserBuilder<T, K> forTabs() {
        this.metadata = CSVFileMetadata.TABS;
        return this;
    }
    
    /**
     * Use comma as separator and double quotes as quote character as per RFC 4180 document.
     * @return this parser builder
     */
    public CSVParserBuilder<T, K> forRfc4180() {
        this.metadata = CSVFileMetadata.RFC_4180;
        return this;
    }
    
    /**
     * Use specified character as field separator.
     * @param separator - field separator character
     * @return this parser builder
     */
    public CSVParserBuilder<T, K> usingSeparatorWithNoQuotes(char separator) {
        this.metadata = new CSVFileMetadata(separator, Optional.empty());
        return this;
    }
    
    /**
     * Use specified characters as field separator and quote character.
     * @param separator - field separator character
     * @param quote - quote character
     * @return this parser builder
     */
    public CSVParserBuilder<T, K> usingSeparatorWithQuote(char separator, char quote) {
        this.metadata = new CSVFileMetadata(separator, Optional.of(quote));
        return this;
    }
    
    /**
     * Buffer size to use when reading from file and parsing. Each buffer is parsed by single thread. 
     * @param size - size in bytes
     * @return this parser builder
     */
    public CSVParserBuilder<T, K> usingBufferSize(int size) {
        this.bufferSize = size;
        return this;
    }
    

    /**
     * Use custom failed record handler to handle exceptions raised during mapping from CSVRecord to T. 
     * @param handler - exception handler
     * @return this parser builder
     */
    public CSVParserBuilder<T, K> usingMappingExceptionHandler(ExceptionHandler handler) {
        this.mappingExceptionHandler = handler;
        return this;
    }
    
    /**
     * Use custom error handler to handle exceptions raised after actual parsing in further stages of the stream. 
     * @param handler - exception handler
     * @return this parser builder
     */
    public CSVParserBuilder<T, K> usingConsumerExceptionHandler(ExceptionHandler handler) {
        this.consumerExceptionHandler = handler;
        return this;
    }
    
    public CSVParser<T> build() {
        return subsetView == null ? 
        		new QuickCSVParser<T,K>(bufferSize, metadata, recordMapper, mappingExceptionHandler, consumerExceptionHandler) :
    			new QuickCSVParser<T,K>(bufferSize, metadata, recordWithHeaderMapper, subsetView, mappingExceptionHandler, consumerExceptionHandler);
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