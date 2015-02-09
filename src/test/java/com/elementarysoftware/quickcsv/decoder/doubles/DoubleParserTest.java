package com.elementarysoftware.quickcsv.decoder.doubles;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.junit.Test;

import com.elementarysoftware.quickcsv.decoder.doubles.DoubleParser;
import com.elementarysoftware.quickcsv.decoder.doubles.JDKDoubleParserAdapter;
import com.elementarysoftware.quickcsv.decoder.doubles.QuickDoubleParser;


public class DoubleParserTest {
	
	@Test
	public void testSimpleCases() {
		doTestSimpleCases(new JDKDoubleParserAdapter());
		doTestSimpleCases(new QuickDoubleParser());
	}
	
	@Test
	public void testBigBuffer() {
		doTestBigBuffer(new JDKDoubleParserAdapter());
		doTestBigBuffer(new QuickDoubleParser());
	}
	
	@Test
	public void testFile() throws Exception {
		doTestFile(new JDKDoubleParserAdapter());
		doTestFile(new QuickDoubleParser());
	}
	
	private void doTestSimpleCases(DoubleParser parser) {
		assertEquals(0.0, parser.parse("0"), 1E-14);
		assertEquals(3.14159265, parser.parse("3.14159265"), 1E-14);
		assertEquals(-93231637.47759183, parser.parse("-93231637.47759183"), 1E-14);
		assertEquals(-0.3903, parser.parse("-0.3903"), 1E-14);
		assertEquals(2.71828183, parser.parse("2.71828183"), 1E-14);
	}
	
	private void doTestBigBuffer(DoubleParser parser) {
		String prefix = "anything";
		String middle = "2.71828183";
		String suffix = "anything again";
		
		byte[] buffer = (prefix + middle + suffix).getBytes();
		double result = parser.parse(buffer, prefix.length(), middle.length());
		assertEquals(2.71828183, result, 1E-14);
	}
	
	
	
	private void doTestFile(DoubleParser parser) throws Exception {
		int nLinesToTest = 500;
		URL fileUrl = getClass().getResource("/cities-dos.txt");
		File file = new File(fileUrl.toURI());
		LineIterator lines = IOUtils.lineIterator(new FileInputStream(file), Charset.defaultCharset());
		int lineNumber = 0;
		while (lines.hasNext() && lineNumber < nLinesToTest) {
		    String[] data = lines.next().split(",");
            for (int i = 0; i < data.length; i++) {
                compareParsingResult(parser, data[i]);
            }
            lineNumber ++;
        }
	}

	private void compareParsingResult(DoubleParser parser, String stringValue) {
		Object d1 = null;
		try {
			d1 = parser.parse(stringValue);
		} catch (Exception e) {
			d1 = e;
		}
		Object d2 = null;
		try {
			d2 = Double.parseDouble(stringValue);
		} catch (Exception e) {
			d2 = e;
		}
		assertEquals(d2.getClass(), d1.getClass());
		if (d2 instanceof Double) {
			assertEquals("Failed for: "+stringValue, d2, d1);
		}
	}
}
