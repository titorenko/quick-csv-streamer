package uk.elementarysoftware.quickcsv.decoder.doubles;

public class DoubleParserFactory {
	
	private static boolean isUseExperimentalParser = true;

	public static DoubleParser getParser() {
		if (isUseExperimentalParser) {
			return new QuickDoubleParser();
		} else {
			return new JDKDoubleParserAdapter();
		}
	}
	
}
