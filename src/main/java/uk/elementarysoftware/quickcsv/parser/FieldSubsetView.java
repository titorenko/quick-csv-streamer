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
 * @param <K> - enum containing list of fields that form the subset
 */
public class FieldSubsetView<K extends Enum<K>> {
    
    private final HeaderSource headerSource;
    private final Class<K> fieldSubset;
    
    private boolean isFirstSlice = true;
    
    private int[] headerIndexesOfK;
    private int[] parseOrderToSourceOrder;
    private int[] fieldSkipSchedule;

    private FieldSubsetView(HeaderSource headerSource, Class<K> fieldSubset) {
        this.headerSource = headerSource;
        this.fieldSubset = fieldSubset;
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
        
        this.fieldSkipSchedule = new int[headerIndexesOfK.length];
        int lastFieldIndex = -1;
        for (int i = 0; i < headerIndexesOfK.length; i++) {
            int idx = headerIndexesOfK[i];
            int nSkip = idx - lastFieldIndex - 1;
            fieldSkipSchedule[i] = nSkip;
            lastFieldIndex = idx;
        }
        
        parseOrderToSourceOrder = new int[getFieldSubsetSize()];
        K[] ks = fieldSubset.getEnumConstants();
        for (int i = 0; i < ks.length; i++) {
            int headerIdx = fieldToHeaderIndex.get(ks[i]);
            parseOrderToSourceOrder[i] = Arrays.binarySearch(headerIndexesOfK, headerIdx);
        }
    }

    private int[] getHeaderIndexesOfK(List<String> header) {
        K[] ks = fieldSubset.getEnumConstants();
        int[] result = new int[ks.length];
        for (int i = 0; i < result.length; i++) {
            if ((result[i] = header.indexOf(ks[i].toString())) == -1) {
                throw new RuntimeException("Field not found in header: "+ks[i].toString());
            }
        }
        Arrays.sort(result);
        return result;
    }

    int[] getFieldIndexes() {
        return headerIndexesOfK;
    }
    
    public Class<K> getFieldSubset() {
		return fieldSubset;
	}

    int[] getFieldSkipSchedule() {
        return fieldSkipSchedule;
    }
    
	List<String> getHeader() {
		return headerSource.getHeader();
	}

    int indexOfInSourceView(int parseIdx) {
        return parseOrderToSourceOrder[parseIdx];
    }
    
    int getFieldSubsetSize() {
        return fieldSubset.getEnumConstants().length;
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