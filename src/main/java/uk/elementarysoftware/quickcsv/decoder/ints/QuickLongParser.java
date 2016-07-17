package uk.elementarysoftware.quickcsv.decoder.ints;
import static uk.elementarysoftware.quickcsv.decoder.ints.ExceptionHelper.*;

public class QuickLongParser implements LongParser {
    
    private static final int radix = 10;
    
    @Override
    public long parse(final byte[] in, final int startIndex, final int len) {

        long result = 0;
        boolean negative = false;
        int index = startIndex;
        long limit = -Long.MAX_VALUE;
        final int end = startIndex + len;
        long multmin;
        int digit;

        if (len > 0) {
            byte firstByte = in[index];
            if (firstByte < '0') { // Possible leading "+" or "-"
                if (firstByte == '-') {
                    negative = true;
                    limit = Long.MIN_VALUE;
                } else if (firstByte != '+')
                    throw nfExceptionFor(in, startIndex, len);

                if (len == 1) // Cannot have lone "+" or "-"
                    throw nfExceptionFor(in, startIndex, len);
                index++;
            }
            multmin = limit / radix;
            while (index < end) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                digit = in[index++] - '0';
                if (digit < 0 || digit >= radix) {
                    throw nfExceptionFor(in, startIndex, len);
                }
                if (result < multmin) {
                    throw nfExceptionFor(in, startIndex, len);
                }
                result *= radix;
                if (result < limit + digit) {
                    throw nfExceptionFor(in, startIndex, len);
                }
                result -= digit;
            }
        } else {
            throw nfExceptionFor(in, startIndex, len);
        }
        return negative ? result : -result;
    }
}