package com.elementarysoftware.quickcsv.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

@FunctionalInterface
public interface CSVParser {
    
    public default Stream<CSVRecord> parse(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        return parse(is);
    }
    
    public Stream<CSVRecord> parse(InputStream is);
}