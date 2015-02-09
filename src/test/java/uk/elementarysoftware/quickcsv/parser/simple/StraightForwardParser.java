package uk.elementarysoftware.quickcsv.parser.simple;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.function.Function;
import java.util.stream.Stream;

import uk.elementarysoftware.quickcsv.api.CSVParser;
import uk.elementarysoftware.quickcsv.api.CSVRecord;
import uk.elementarysoftware.quickcsv.api.Field;

public class StraightForwardParser implements CSVParser {
	
    @Override @SuppressWarnings("resource")
    public Stream<CSVRecord> parse(File source) throws IOException {
        Stream<String> lines = Files.lines(source.toPath());
        return lines.map(l -> l.split(",")).map(toCSVRecord());
    }
    
	private Function<String[], CSVRecord> toCSVRecord() {
		return new Function<String[], CSVRecord>() {
			@Override
			public CSVRecord apply(String[] fields) {
				return new SimpleCSVRecord(fields);
			}
		};
	}

	public static class SimpleCSVRecord implements CSVRecord {

		private String[] fields;
		private int index;

		public SimpleCSVRecord(String[] fields) {
			this.index = 0;
			this.fields = fields;
		}

		@Override
		public void skipField() {
			index++;
		}

		@Override
		public void skipFields(int nFields) {
			index+=nFields;
		}

		@Override
		public Field getNextField() {
			return new SimpleField(fields[index++]);
		}

	}
	
	public static class SimpleField implements Field {
		String value;
		
		public SimpleField(String value) {
			this.value = value;
		}

		@Override
		public ByteBuffer raw() {
			return null;
		}

		@Override
		public String asString() {
			return value;
		}

		@Override
		public double asDouble() {
			return Double.parseDouble(value);
		}

		@Override
		public byte asByte() {
			return 0;
		}

		@Override
		public char asChar() {
			return 0;
		}

		@Override
		public short asShort() {
			return 0;
		}

		@Override
		public int asInt() {
		    if (isEmpty()) return 0;
			return Integer.parseInt(value);
		}

		@Override
		public long asLong() {
			return 0;
		}
		
		@Override
		public Field clone() {
		    return this;
		}

        @Override
        public boolean isEmpty() {
            return value.length() == 0;
        }
	}

    @Override
    public Stream<CSVRecord> parse(InputStream is) {
        throw new UnsupportedOperationException();
    }
}