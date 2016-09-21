package uk.elementarysoftware.quickcsv.parser;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import uk.elementarysoftware.quickcsv.api.CSVParserBuilder;
import uk.elementarysoftware.quickcsv.api.StandardMappers;
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
		Stream<City> cities = CSVParserBuilder.aParser(ignoreErrors(City.MAPPER))
				.build().parse(input).sequential();
		String[] actual = cities.filter(c -> c != null).map(c -> c.toString()).toArray(String[]::new);
		assertArrayEquals(expected, actual);
	}
	
    @Test
    public void testSequentialWithEnumApi() throws Exception {
        Stream<City> cities = CSVParserBuilder.aParser(City.HeaderAwareMapper.MAPPER, City.HeaderAwareMapper.Fields.class) 
                .build().parse(input).sequential();
        String[] actual = cities.map(c -> c.toString()).toArray(String[]::new);
        assertArrayEquals(expected, actual);
    }
    
    @Test
    public void testSequentialWithEnumApiWithFirstColumn() throws Exception {
        Stream<City> cities = CSVParserBuilder.aParser(City.HeaderAwareMapper2.MAPPER, City.HeaderAwareMapper2.Fields.class) 
                .build().parse(input).sequential();
        String[] actual = cities.map(c -> c.toString()).toArray(String[]::new);
        assertEquals(3, actual.length);
    }
	
	@Test
	public void testParallel() throws Exception {
		Stream<City> cities = CSVParserBuilder.aParser(ignoreErrors(City.MAPPER))
				.build().parse(input).parallel();
		String[] actual = cities.filter(c -> c != null).map(c -> c.toString()).toArray(String[]::new);
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
	
	private static <T,S> Function<T, S> ignoreErrors(Function<T, S> f) {
        return t -> {
          try {
              return f.apply(t);
          } catch (Exception e) {
              return null;
          }
        };
    }
}