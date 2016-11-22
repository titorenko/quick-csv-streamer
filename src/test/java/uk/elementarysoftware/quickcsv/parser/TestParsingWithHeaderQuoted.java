package uk.elementarysoftware.quickcsv.parser;

import static org.junit.Assert.*;

import java.io.File;
import java.util.stream.Stream;

import org.junit.Test;

import uk.elementarysoftware.quickcsv.api.CSVParserBuilder;
import uk.elementarysoftware.quickcsv.sampledomain.City;

public class TestParsingWithHeaderQuoted {

    File input = new File("src/test/resources/cities-with-header-quoted.txt");

    String[] expected = new String[] {
            "City [city=Andorra, population=0, latitude=42.5, longitude=1.5166667]",
            "City [city=City of London, population=0, latitude=51.514125, longitude=-0.093689]",
            "City [city=Kharkiv, population=0, latitude=49.980814, longitude=36.252718]"
    };


    @Test
    public void testSequentialWithEnumApi() throws Exception {
        Stream<City> cities = CSVParserBuilder.aParser(City.HeaderAwareMapper.MAPPER, City.HeaderAwareMapper.Fields.class)
                .usingSeparatorWithQuote(',', '"')
                .build().parse(input).sequential();
        String[] actual = cities.map(c -> c.toString()).toArray(String[]::new);
        assertArrayEquals(expected, actual);
    }

}
