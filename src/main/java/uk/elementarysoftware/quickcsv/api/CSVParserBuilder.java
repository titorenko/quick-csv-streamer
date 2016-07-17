package uk.elementarysoftware.quickcsv.api;

import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import uk.elementarysoftware.quickcsv.parser.FieldSubsetView;
import uk.elementarysoftware.quickcsv.parser.QuickCSVParser;

/**
 * CSV Parser builder, use this class to construct {@link CSVParser}.
 * 
 * @param <T> - type of object that each record of the CSV data will be mapped to
 * @param <K> - type of enumeration that is used to specify fields to be parsed, only relevant for header-aware parser. 
 */
public class CSVParserBuilder<T, K extends Enum<K>> {
    
    private int bufferSize = 512*1024;
    
    private CSVFileMetadata metadata = CSVFileMetadata.RFC_4180;

	private Function<CSVRecord, T> recordMapper;

	private Function<CSVRecordWithHeader<K>, T> recordWithHeaderMapper;
	private FieldSubsetView<K> subsetView = null;
	
	private Charset charset = Charset.defaultCharset();
    
    private CSVParserBuilder() {
	}

    /**
     * Create new parser using supplied mapping function. 
     * 
     * Mapping function can not store reference to {@link CSVRecord} object, 
     * it needs to be a pure function that creates new instance of T. 
     * CSVRecord could be mutated by the parser when next field or record are processed.
     * 
     * @param mapper - mapping function from CSVRecord to T
     * @param <T> - type of object that each record of the CSV data will be mapped to
     * @param <K> - ignored
     * @return this parser builder
     */
	public static <T, K extends Enum<K>> CSVParserBuilder<T, K> aParser(Function<CSVRecord, T> mapper) {
        CSVParserBuilder<T, K> builder = new CSVParserBuilder<T, K>();
        builder.recordMapper = mapper;
        return builder;
    }
	
	/**
	 * Create new header-aware parser using supplied mapping function. 
	 * 
	 * Mapping function can not store reference to {@link CSVRecordWithHeader} object, 
	 * it needs to be a pure function that create new instance of T.
	 *  
     * CSVRecordWithHeader could be mutated by the parser when next record is processed.
	 * 
	 * @param mapper - mapping function from CSVRecordWithHeader to T
	 * @param fields - enumeration specifying fields that should be parsed
	 * @param <T> - type of object that each record of the CSV data will be mapped to
     * @param <K> - type of enumeration that is used to specify fields to be parsed
     * 
	 * @return this parser builder
	 */
	
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
     * Quote character can be escaped by preceding it with another quote character.
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
     * Specifies charset to use during parsing. By default Charset.defaultCharset() is used.
     * This parser only supports charset that represent separators and digits as single bytes.
     * @param charset - charset to use during parsing
     * @return this parser builder
     */
    public CSVParserBuilder<T, K> usingCharset(Charset charset) {
        this.charset = charset;
        return this;
    }
    
    /**
     * Specifies charset name to use during parsing. By default Charset.defaultCharset() is used.
     * This parser only supports charset that represent separators and digits as single bytes.
     * @param charsetName - charset to use during parsing
     * @return this parser builder
     */
    public CSVParserBuilder<T, K> usingCharset(String charsetName) {
        return usingCharset(Charset.forName(charsetName));
    }
    
    /**
     * Construct parser using current setting
     * @return CSV Parser
     */
    public CSVParser<T> build() {
        return subsetView == null ? 
        		new QuickCSVParser<T,K>(bufferSize, metadata, recordMapper, charset) :
    			new QuickCSVParser<T,K>(bufferSize, metadata, recordWithHeaderMapper, subsetView, charset);
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