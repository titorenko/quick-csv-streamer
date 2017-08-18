package uk.elementarysoftware.quickcsv.parser;

import static org.junit.Assert.assertArrayEquals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import uk.elementarysoftware.quickcsv.api.CSVParser;
import uk.elementarysoftware.quickcsv.api.CSVParserBuilder;

public class TestParsingSpecialCases {
	
	CSVParser<String[]> parser = 
			CSVParserBuilder.aParser(r -> new String[] {
					r.getField(Fields.A).asString(), 
					r.getField(Fields.B).asString(), 
					r.getField(Fields.C).asString()
			},  Fields.class).build();
	
	@Test
	public void testLineEndsWithEmptyField() {
		InputStream csv = new ByteArrayInputStream("A,B,C\na,,".getBytes());
		List<String[]> result = parser.parse(csv).collect(Collectors.toList());
		assertArrayEquals(new String[] {"a", "", ""}, result.get(0));
	}
	
	@Test
	public void testLineEndsWithEmptyFieldQuoted() {
		InputStream csv = new ByteArrayInputStream("\"A\",\"B\",\"C\"\n\"a\",\"\",\"\"".getBytes());
		List<String[]> result = parser.parse(csv).collect(Collectors.toList());
		assertArrayEquals(new String[] {"a", "", ""}, result.get(0));
	}
	
	static enum Fields {
		A, B, C;
	}
}
