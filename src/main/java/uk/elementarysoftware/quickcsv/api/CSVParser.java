package uk.elementarysoftware.quickcsv.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

public interface CSVParser<T> {
    
    public default Stream<T> parse(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        return parse(is);
    }
    
    public Stream<T> parse(InputStream is);
    
    public Stream<T> parse(ByteArraySource bas);
}