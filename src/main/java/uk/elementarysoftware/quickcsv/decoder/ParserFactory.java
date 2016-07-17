package uk.elementarysoftware.quickcsv.decoder;

import uk.elementarysoftware.quickcsv.decoder.doubles.DoubleParser;
import uk.elementarysoftware.quickcsv.decoder.doubles.JDKDoubleParserAdapter;
import uk.elementarysoftware.quickcsv.decoder.doubles.QuickDoubleParser;
import uk.elementarysoftware.quickcsv.decoder.ints.IntParser;
import uk.elementarysoftware.quickcsv.decoder.ints.LongParser;
import uk.elementarysoftware.quickcsv.decoder.ints.QuickIntParser;
import uk.elementarysoftware.quickcsv.decoder.ints.QuickLongParser;

class ParserFactory {
    
    private final boolean useQuickParsers;

    ParserFactory() {
        this.useQuickParsers = "true".equals(System.getProperty("uk.elementarysoftware.useQuickParsers", "true"));
    }
	
	public DoubleParser getDoubleParser() {
		if (useQuickParsers) {
			return new QuickDoubleParser();
		} else {
			return new JDKDoubleParserAdapter();
		}
	}

    public IntParser getIntParser() {
        return new QuickIntParser();
    }

    public LongParser getLongParser() {
        return new QuickLongParser();
    }
}