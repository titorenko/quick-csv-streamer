package uk.elementarysoftware.quickcsv.api;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class StandardMappers {
    /**
     * Just convert to string list. Note that is NOT recommended to use this function in high volume scenarios,
     * more effective is to directly convert to domain object or array.
     */
    public static final Function<CSVRecord, List<String>> TO_STRING_LIST = new Function<CSVRecord, List<String>>() {

        @Override
        public List<String> apply(CSVRecord r) {
            List<String> result = new ArrayList<>();
            while(true) {
                Field f = r.getNextField();
                if (f == null) break;
                result.add(f.asString());
            }
            return result;
        }
        
    };
}
