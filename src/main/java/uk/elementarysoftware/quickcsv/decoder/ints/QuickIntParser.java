package uk.elementarysoftware.quickcsv.decoder.ints;
import static uk.elementarysoftware.quickcsv.decoder.ints.ExceptionHelper.*;

public class QuickIntParser implements IntParser {
    
    private static final int radix = 10;

    @Override
    public int parse(final byte[] in, final int startIndex, final int len) {
        
        int result = 0;
        boolean negative = false;
        int index = startIndex;
        final int end = startIndex + len;
        int limit = -Integer.MAX_VALUE;
        int multmin;
        int digit;

        if (len > 0) {
            byte firstByte = in[index];
            if (firstByte < '0') { // Possible leading "+" or "-"
                if (firstByte == '-') {
                    negative = true;
                    limit = Integer.MIN_VALUE;
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