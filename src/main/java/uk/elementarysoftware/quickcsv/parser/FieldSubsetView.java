package uk.elementarysoftware.quickcsv.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import uk.elementarysoftware.quickcsv.api.CSVParserBuilder.CSVFileMetadata;

/**
 * Provides view on the CSVRecord that focuses on particular subset of fields.
 * 
 * Within the view fields can be accessed by index in order of the subset or by field enumeration K.   
 * @param <K>
 */
public class FieldSubsetView<K extends Enum<K>> {
    
    private final HeaderSource headerSource;
    private final Class<K> fieldSubset;
    
    private boolean isFirstSlice = true;
    
    private int[] headerIndexesOfK;
    private int[] parseOrderToSourceOrder;
    private final Map<K, Integer> kToSourceOrder;

    private FieldSubsetView(HeaderSource headerSource, Class<K> fieldSubset) {
        this.headerSource = headerSource;
        this.fieldSubset = fieldSubset;
        this.kToSourceOrder = new EnumMap<K, Integer>(fieldSubset);//TODO: move to simple array
    }
    
    public static <K extends Enum<K>> FieldSubsetView<K> forExplicitHeader(Class<K> fieldsToSource, String... header) {
        return new FieldSubsetView<>(new HeaderSource.ExplicitHeader(header), fieldsToSource);
    }
    
    public static <K extends Enum<K>> FieldSubsetView<K> forSourceSuppliedHeader(Class<K> fieldsToSource) {
        return forSourceSuppliedHeader(fieldsToSource, 0);
    }
    
    public static <K extends Enum<K>> FieldSubsetView<K>  forSourceSuppliedHeader(Class<K> fieldsToSource, int headerRowIndexInFile) {
        return new FieldSubsetView<>(new HeaderSource.SourceSuppliedHeader(headerRowIndexInFile), fieldsToSource);
    }
    
    public void onSlice(ByteSlice slice, CSVFileMetadata metadata) {
        if (isFirstSlice) {
            headerSource.onSlice(slice, metadata);
            initLookups();
            isFirstSlice = false;
        }
    }
    
    private void initLookups() {
        List<String> header = headerSource.getHeader();
        headerIndexesOfK = getHeaderIndexesOfK(header);
        Map<K, Integer> fieldToHeaderIndex = new EnumMap<K, Integer>(fieldSubset);
        for (K k : fieldSubset.getEnumConstants()) {
            fieldToHeaderIndex.put(k, header.indexOf(k.toString()));
        }
        parseOrderToSourceOrder = new int[fieldSubset.getEnumConstants().length];
        
        K[] ks = fieldSubset.getEnumConstants();
        for (int i = 0; i < ks.length; i++) {
            int headerIdx = fieldToHeaderIndex.get(ks[i]);
            parseOrderToSourceOrder[i] = Arrays.binarySearch(headerIndexesOfK, headerIdx);
            kToSourceOrder.put(ks[i], parseOrderToSourceOrder[i]);
        }
    }

    private int[] getHeaderIndexesOfK(List<String> header) {
        K[] ks = fieldSubset.getEnumConstants();
        int[] result = new int[ks.length];
        for (int i = 0; i < result.length; i++) {
            if ((result[i] = header.indexOf(ks[i].toString())) == -1) {
                throw new RuntimeException("field not found in header: "+ks[i].name());
            }
        }
        Arrays.sort(result);
        return result;
    }

    public Class<K> getFieldSubset() {
		return fieldSubset;
	}
    
    public int[] getFieldIndexes() {
        return headerIndexesOfK;
    }

	public List<String> getHeader() {
		return headerSource.getHeader();
	}

    public int indexOfInSourceView(K fieldName) {
        return kToSourceOrder.get(fieldName);
    }
    
    public int indexOfInSourceView(int parseIdx) {
        return parseOrderToSourceOrder[parseIdx];
    }
    
    public static abstract class HeaderSource {
        
        private HeaderSource() {}
        
        abstract void onSlice(ByteSlice slice, CSVFileMetadata metadata);
        abstract List<String> getHeader();

        private static class ExplicitHeader extends HeaderSource {
            private final String[] header;

            public ExplicitHeader(String[] header) {
                this.header = header;
            }
            
            @Override
            List<String> getHeader() {
                return Arrays.asList(header);
            }
            
            @Override 
            void onSlice(ByteSlice slice, CSVFileMetadata metadata) {}
        }
        
        private static class SourceSuppliedHeader extends HeaderSource {
            
            private final int headerIndex;
            private List<String> header;

            public SourceSuppliedHeader(int headerIndex) {
                this.headerIndex = headerIndex;
            }
            
            @Override 
            void onSlice(ByteSlice slice, CSVFileMetadata metadata) {
                for (int i = 0; i < headerIndex; i++) {
                    slice.nextLine();
                }
                List<String> header = new ArrayList<>();
                ByteArrayField field;
                while((field = slice.getNextField(metadata)) != null) {
                    header.add(field.asString());
                }
                this.header = header;
            }

            @Override
            List<String> getHeader() {
                return header;
            }
        }
    }
}