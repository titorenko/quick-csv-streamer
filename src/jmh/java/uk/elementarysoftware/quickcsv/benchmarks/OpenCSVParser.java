package uk.elementarysoftware.quickcsv.benchmarks;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.io.IOUtils;

import com.opencsv.CSVReader;


public class OpenCSVParser {
    
    public Stream<City> parse(InputStream is) {
        Reader reader = new InputStreamReader(is);
        CSVReader csvReader = new CSVReader(reader);
        Iterator<City> iterator = new Iterator<City>() {
            private boolean isEndReached = false;
            
            @Override
            public boolean hasNext() {
                return !isEndReached;
            }

            @Override
            public City next() {
                try {
                    String[] values = csvReader.readNext();
                    if (values == null) {
                        isEndReached = true;
                        return null;
                    } else {
                        return toCity(values);
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        };
        Spliterator<City> spliterator = Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED);
        return StreamSupport.stream(spliterator, false).onClose(new Runnable() {
            @Override
            public void run() {
                IOUtils.closeQuietly(csvReader);
            }
        });
    }

    protected City toCity(String[] values) {
        if (values.length < 7) return null;
        return new City(values[2], parseInt(values[4]), parseDouble(values[5]), parseDouble(values[6]));
    }

    private int parseInt(String value) {
        try {
            return value.isEmpty() ? 0 : Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
    }

    private double parseDouble(String value) {
        return value.isEmpty() ? 0 : Double.parseDouble(value);
    }

}
