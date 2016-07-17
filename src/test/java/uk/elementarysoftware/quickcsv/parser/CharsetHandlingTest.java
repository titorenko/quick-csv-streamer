package uk.elementarysoftware.quickcsv.parser;

import static org.junit.Assert.*;

import java.io.File;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.Test;

import uk.elementarysoftware.quickcsv.api.CSVParserBuilder;
import uk.elementarysoftware.quickcsv.api.CSVRecordWithHeader;
import uk.elementarysoftware.quickcsv.sampledomain.City;

public class CharsetHandlingTest {
    
    File utf8input = new File("src/test/resources/cities-rus-utf8.txt");
    File cp1251input = new File("src/test/resources/cities-rus-cp1251.txt");
    
    String[] expected = new String[] {
            "City [city=Андора, population=0, latitude=42.5, longitude=1.5166667]",
            "City [city=City of London, population=0, latitude=51.514125, longitude=-0.093689]",
            "City [city=Харків, population=0, latitude=49.980814, longitude=36.252718]" 
    };
    
    @Test
    public void testUtf8() throws Exception {
        Stream<City> cities = CSVParserBuilder.aParser(EnumMapper.MAPPER, EnumMapper.RusFields.class) //TODO add that example to docs 
                .usingCharset("UTF-8").build().parse(utf8input);
        String[] actual = cities.map(c -> c.toString()).toArray(String[]::new);
        assertArrayEquals(expected, actual);
    }
    
    @Test
    public void testCp1251() throws Exception {
        Stream<City> cities = CSVParserBuilder.aParser(EnumMapper.MAPPER, EnumMapper.RusFields.class)
                .usingCharset("Cp1251").build().parse(cp1251input);
        String[] actual = cities.map(c -> c.toString()).toArray(String[]::new);
        assertArrayEquals(expected, actual);
    }
    
    public static class EnumMapper { 
        
        enum RusFields { //TODO add that example to docs
            Latitude("Широта"),
            Longitude("Долгота"),
            AccentCity("Город"),
            Population("Население");
            
            private final String headerFieldName;

            private RusFields(String headerFieldName) {
                this.headerFieldName = headerFieldName;
            }
            
            @Override
            public String toString() {
                return headerFieldName;
            }
        }
        
        public static final Function<CSVRecordWithHeader<RusFields>, City> MAPPER = r -> {
            return new City(
                    r.getField(RusFields.AccentCity).asString(),
                    r.getField(RusFields.Population).asInt(),
                    r.getField(RusFields.Latitude).asDouble(),
                    r.getField(RusFields.Longitude).asDouble(),
                    r.getField(RusFields.Population).asLong()
            );
        };
    }
}