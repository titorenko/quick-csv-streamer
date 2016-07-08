package uk.elementarysoftware.quickcsv.integration;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;

import uk.elementarysoftware.quickcsv.api.CSVParserBuilder;
import uk.elementarysoftware.quickcsv.api.StandardMappers;

public class CorrectnessTest {
    
    File input = new File("src/test/resources/correctness.txt");
    
    @Test
    @SuppressWarnings("unchecked")
    public void testParse() throws IOException {
        Stream<List<String>> stream = CSVParserBuilder.aParser(StandardMappers.TO_STRING_LIST).build().parse(input);
        List<String>[] rows = stream.toArray(List[]::new);
        assertArrayEquals(new String[] {"Year", "Make", "Model", "Description", "Price"}, rows[0].toArray(new String[0]));
        assertArrayEquals(new String[] {"1997", "Ford", "E350", "ac, abs, moon", "3000.00"}, rows[1].toArray(new String[0]));
        assertArrayEquals(new String[] {"1999", "Chevy", "Venture \"Extended Edition\"", "", "4900.00"}, rows[2].toArray(new String[0]));
        String separ = System.getProperty("line.separator");
        assertArrayEquals(new String[] {"1996", "Jeep", "Grand Cherokee", "MUST SELL!"+separ+"air, moon roof, loaded", "4799.00"}, rows[3].toArray(new String[0]));
        assertArrayEquals(new String[] {"1999", "Chevy", "Venture \"Extended Edition, Very Large\"", "", "5000.00"}, rows[4].toArray(new String[0]));
        assertArrayEquals(new String[] {"", "", "Venture \"Extended Edition\"", "", "4900.00" }, rows[5].toArray(new String[0]));
    }

}
