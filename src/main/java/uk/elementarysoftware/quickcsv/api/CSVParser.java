package uk.elementarysoftware.quickcsv.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

/**
 * CSV Parser can parse inputs such as {@link InputStream} or more generally {@link ByteArraySource} to Stream&lt;T&gt;.
 * 
 * @param <T> - the type of the parsing result
 */
public interface CSVParser<T> {
    
    public default Stream<T> parse(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        return parse(is);
    }
    
    public Stream<T> parse(InputStream is);
    
    public Stream<T> parse(ByteArraySource bas);
}