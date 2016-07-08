package uk.elementarysoftware.quickcsv.integration;

import static org.junit.Assert.*;

import java.io.File;
import java.util.stream.Stream;

import org.junit.Test;

import uk.elementarysoftware.quickcsv.api.CSVParserBuilder;
import uk.elementarysoftware.quickcsv.parser.simple.StraightForwardParser;
import uk.elementarysoftware.quickcsv.sampledomain.City;

public class IntegrationTest {
    
    File inputDos = new File("src/test/resources/cities-dos.txt");
    File inputUnix = new File("src/test/resources/cities-unix.txt");
    
    int[] bufferSizesToTest = new int[] {1024, 11_111, 1_000_000};
    
    
    @Test
    public void testMultiThreaded() throws Exception {
        Stream<City> s1 = new StraightForwardParser().parse(inputDos).map(City.MAPPER);
        Object[] expected = s1.toArray();
        for (int i = 0; i < bufferSizesToTest.length; i++) {
            Stream<City> s2 = CSVParserBuilder.aParser(City.MAPPER).usingBufferSize(bufferSizesToTest[i]).build().parse(inputDos);
            assertArrayEquals(expected, s2.toArray());
        }
    }
    
    @Test
    public void testSingleThreaded() throws Exception {
        Stream<City> s1 = new StraightForwardParser().parse(inputDos).map(City.MAPPER);
        Stream<City> s2 = CSVParserBuilder.aParser(City.MAPPER).build().parse(inputDos).sequential();
        assertArrayEquals(s1.toArray(), s2.sequential().toArray());
    }
    
    @Test
    public void testDosVsUnix() throws Exception {
        Stream<City> s1 = CSVParserBuilder.aParser(City.MAPPER).build().parse(inputUnix);
        Stream<City> s2 = CSVParserBuilder.aParser(City.MAPPER).build().parse(inputDos);
        assertArrayEquals(s1.toArray(), s2.sequential().toArray());
    }
    
}