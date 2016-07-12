package uk.elementarysoftware.quickcsv.parser;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import uk.elementarysoftware.quickcsv.api.CSVParserBuilder;
import uk.elementarysoftware.quickcsv.api.StandardMappers;
import uk.elementarysoftware.quickcsv.sampledomain.City;
import uk.elementarysoftware.quickcsv.sampledomain.City2;

public class TestParsingWithHeader {
	
	File input = new File("src/test/resources/cities-with-header.txt");
	
	String[] expected = new String[] {
			"City [city=Andorra, population=0, latitude=42.5, longitude=1.5166667]",
			"City [city=City of London, population=0, latitude=51.514125, longitude=-0.093689]",
			"City [city=Kharkiv, population=0, latitude=49.980814, longitude=36.252718]" 
	};

	@Test
	public void testSequential() throws Exception {
		Stream<City> cities = CSVParserBuilder.aParser(City.MAPPER).usingMappingExceptionHandler((ex, r) -> {})
				.build().parse(input).sequential();
		String[] actual = cities.map(c -> c.toString()).toArray(String[]::new);
		assertArrayEquals(expected, actual);
	}
	
	@Test
    public void testSequentialWithEnumApi() throws Exception {
        Stream<City2> cities = CSVParserBuilder.aParser(City2.MAPPER, City2.Fields.class) 
                .build().parse(input).sequential();
        String[] actual = cities.map(c -> c.toString()).toArray(String[]::new);
        assertArrayEquals(expected, actual);
    }
	
	@Test
	public void testParallel() throws Exception {
		Stream<City> cities = CSVParserBuilder.aParser(City.MAPPER).usingMappingExceptionHandler((ex, r) -> {})
				.build().parse(input).parallel();
		String[] actual = cities.map(c -> c.toString()).toArray(String[]::new);
		assertArrayEquals(expected, actual);
	}
	
	@Test
	/**
	 * Checks that we can skip records on parallel stream. That verifies that the stream is ordered by 
	 * default and behaves normally when being copied by java's skipping stream decorator. 
	 */
    public void testParallelParseWithSkip() throws IOException {
		List<List<String>> result = CSVParserBuilder.aParser(StandardMappers.TO_STRING_LIST).build()
        		.parse(input).skip(1).collect(Collectors.toList());
		assertEquals(3, result.size());
		assertArrayEquals(new String[] {"ad","andorra","Andorra","07","","42.5","1.5166667"}, result.get(0).toArray(new String[0]));
    }
}