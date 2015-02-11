package uk.elementarysoftware.quickcsv.parser;

import java.io.File;
import java.util.stream.Stream;

import org.junit.Test;
import static org.junit.Assert.*;

import uk.elementarysoftware.quickcsv.api.CSVParserBuilder;
import uk.elementarysoftware.quickcsv.sampledomain.City;

public class TestParsingWithHeader {
	
	File input = new File("src/test/resources/cities-with-header.txt");
	
	String[] expected = new String[] {
			"City [city=Andorra, population=0, latitude=42.5, longitude=1.5166667]",
			"City [city=City of London, population=0, latitude=51.514125, longitude=-0.093689]",
			"City [city=Kharkiv, population=0, latitude=49.980814, longitude=36.252718]" 
	};

	@Test
	public void testSequential() throws Exception {
		Stream<City> cities = CSVParserBuilder.aParser().build().parse(input)
				.sequential().skip(1).map(City.MAPPER);
		String[] actual = cities.map(c -> c.toString()).toArray(String[]::new);
		assertArrayEquals(expected, actual);
	}

	@Test
	public void testSequentialViaAPI() throws Exception {
		Stream<City> cities = CSVParserBuilder.aParser().skipFirstRecord()
				.build().parse(input).sequential().map(City.MAPPER);
		String[] actual = cities.map(c -> c.toString()).toArray(String[]::new);
		assertArrayEquals(expected, actual);
	}

	@Test
	public void testParallel() throws Exception {
		Stream<City> cities = CSVParserBuilder.aParser().skipFirstRecord()
				.build().parse(input).parallel().map(City.MAPPER);
		String[] actual = cities.map(c -> c.toString()).toArray(String[]::new);
		assertArrayEquals(expected, actual);
	}
}