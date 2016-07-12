package uk.elementarysoftware.quickcsv.parser;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class FieldSubsetViewTest {
	
	enum FieldSubset {
		C3, C4, C1
	}

	private FieldSubsetView<FieldSubset> fs;
	
	@Before
	public void init() {
		this.fs = FieldSubsetView.forExplicitHeader(FieldSubset.class, "C1", "C2", "C3", "C4", "C5");
		fs.onSlice(null, null);
	}
	
	@Test
	public void testFieldIndexIsSortedAndCorrect() {
		assertArrayEquals(new int[] {0, 2, 3}, fs.getFieldIndexes());
	}
	
	@Test
	public void testIndexOfEnumValuesInSourceView() {
		assertEquals(1, fs.indexOfInSourceView(FieldSubset.C3));
		assertEquals(2, fs.indexOfInSourceView(FieldSubset.C4));
		assertEquals(0, fs.indexOfInSourceView(FieldSubset.C1));
	}
	
	@Test
	public void testIndexOfInSourceView() {
		assertEquals(1, fs.indexOfInSourceView(0));
		assertEquals(2, fs.indexOfInSourceView(1));
		assertEquals(0, fs.indexOfInSourceView(2));
	}
}
