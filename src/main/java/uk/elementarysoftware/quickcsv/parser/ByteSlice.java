package uk.elementarysoftware.quickcsv.parser;

import uk.elementarysoftware.quickcsv.api.Field;
import uk.elementarysoftware.quickcsv.tuples.Pair;


public interface ByteSlice {
    static final byte CR = 0xD;
    static final byte LF = 0xA;
    
    public static ByteSlice wrap(byte[] buffer) {
        return wrap(buffer, buffer.length);
    }
    
    public static ByteSlice wrap(byte[] buffer, int length) {
        return new SingleByteSlice(buffer, length);
    }

    public static ByteSlice empty() {
        return wrap(new byte[0]);
    }

    public static ByteSlice join(ByteSlice prefix, ByteSlice suffix) {
        return new CompositeByteSlice((SingleByteSlice) prefix, (SingleByteSlice) suffix);
    }
    
    public Pair<ByteSlice, ByteSlice> splitOnLastLineEnd();
    
    public boolean nextLine();
    
    /**
     * Skip until next occurrence of c character. False if not found and end of slice is reached 
     */
    public boolean skipUntil(final char c);
    
    public boolean skipUntil(final char c, final char quote);
    
    /**
     * Returns next field and advances to next field. Returns null when end of line or end of slice is reached.
     */
    public Field nextField(final char c);
    
    public Field nextField(final char c, final char quote);

    public int size();
    
    public boolean hasMoreData();

    default public boolean isEmpty() { 
        return !hasMoreData();
    }
}

class SingleByteSlice implements ByteSlice {
    final int start;//inclusive
    final int end;//exclusive
    final byte[] buffer;
    final ByteArrayField fieldTemplateObject;
    
    int currentIndex;
    
    public SingleByteSlice(byte[] buffer, int length) {
        this(buffer, 0, length);
    }
    
    public SingleByteSlice(byte[] buffer, int start, int end) {
        this.buffer = buffer;
        this.start = start;
        this.end = end;
        this.fieldTemplateObject = new ByteArrayField(buffer, 0, 0);
        this.currentIndex = start;
    }

    @Override
    public int size() {
        return end - start;
    }

    @Override
    public boolean hasMoreData() {
        return currentIndex < end;
    }
    
    boolean frontTrim() {
        boolean seenEOL = false;
        for(; hasMoreData() && (buffer[currentIndex]==CR || buffer[currentIndex]==LF); currentIndex++) {
            seenEOL = true;
        }
        return seenEOL;
    }
    
    @Override
    public boolean nextLine() {
        for(; hasMoreData() && buffer[currentIndex]!=CR && buffer[currentIndex]!=LF; currentIndex++);
        return frontTrim();
    }
    
    public Pair<ByteSlice, ByteSlice> splitOnLastLineEnd() {
        int i = end-1;
        for (;i >=currentIndex && buffer[i] != LF; i--);
        SingleByteSlice prefix = new SingleByteSlice(buffer, currentIndex, i+1);
        SingleByteSlice suffix = new SingleByteSlice(buffer, i+1, end);
        return Pair.of(prefix, suffix);
    }
    
    public boolean skipUntil(final char c) {
    	boolean isFound = false;
        while(currentIndex < end) {
            if (buffer[currentIndex]==c) {
                currentIndex++;
                isFound = true;
                break;
            }
            currentIndex++;
        }
        return isFound;
    }
    
    public boolean skipUntil(char c, char q) {
        boolean inQuote = currentIndex < buffer.length && buffer[currentIndex] == q;
        if (!inQuote) return skipUntil(c);
        currentIndex++;
        boolean isFound = false;
        while(currentIndex < end) {
            if (buffer[currentIndex]==c && buffer[currentIndex-1] == q) {
                currentIndex++;
                isFound = true;
                break;
            }
            currentIndex++;
        }
        return isFound;
    }

    public Field nextField(final char c) {
        int startIndex = currentIndex;
        int endIndex = currentIndex;
        while(currentIndex < end) {
            byte cur = buffer[currentIndex];
            if (cur == c || cur == CR || cur == LF) {
                endIndex = currentIndex;
                if (cur == c) 
                    currentIndex++;
                break;
            } else {
                currentIndex++;
            }
        }
        if (currentIndex == startIndex) return null;
        if (currentIndex == end) endIndex = end;
        fieldTemplateObject.modifyBounds(startIndex, endIndex);
        return fieldTemplateObject;
    }
    
    @Override
    public Field nextField(char c, char q) {
        boolean inQuote = currentIndex < buffer.length && buffer[currentIndex] == q;
        if (!inQuote) return nextField(c);
        currentIndex++;
        int startIndex = currentIndex;
        int endIndex = currentIndex;
        while(currentIndex < end) {
            byte cur = buffer[currentIndex];
            if (cur == c && buffer[currentIndex-1] == q) {//there is an issue when we have escaped quote and then separator, but we ignore it for now
                endIndex = currentIndex - 1;
                currentIndex++;
                break;
            } else {
                currentIndex++;
            }
        }
        if (currentIndex == startIndex) return null;
        if (currentIndex == end) {
            if (buffer[end-1] == q) endIndex = end - 1; else endIndex = end;
        }
        fieldTemplateObject.modifyBounds(startIndex, endIndex, q);
        return fieldTemplateObject;
    }
    
    @Override
    public String toString() {
        return new String(buffer, start, size());
    }
}

class CompositeByteSlice implements ByteSlice {

    private final SingleByteSlice prefix;
    private final SingleByteSlice suffix;
    private final ByteArrayField prefixFieldTemplateObject;
    private final ByteArrayField suffixFieldTemplateObject;

    CompositeByteSlice(SingleByteSlice prefix, SingleByteSlice suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
        this.prefixFieldTemplateObject = new ByteArrayField(prefix.buffer, 0, 0);
        this.suffixFieldTemplateObject = new ByteArrayField(suffix.buffer, 0, 0);
    }

    @Override
    public Pair<ByteSlice, ByteSlice> splitOnLastLineEnd() {
        Pair<ByteSlice, ByteSlice> sliced = suffix.splitOnLastLineEnd();
        return Pair.of(ByteSlice.join(this.prefix, sliced.first), sliced.second);
    }

    @Override
    public int size() {
        return prefix.size() + suffix.size();
    }

    @Override
    public boolean hasMoreData() {
        return prefix.hasMoreData() || suffix.hasMoreData();
    }

    @Override
    public boolean skipUntil(char c) {
        if (prefix.isEmpty()) return suffix.skipUntil(c);
        boolean isFound = prefix.skipUntil(c);
        if (isFound) {
            return true;
        } else {
            return suffix.skipUntil(c);
        }
    }
    
    @Override
    public boolean skipUntil(char c, char q) {
        if (prefix.isEmpty()) return suffix.skipUntil(c, q);
        boolean isFound = prefix.skipUntil(c, q);
        if (isFound) {
            return true;
        } else {
            return suffix.skipUntil(c, q);
        }
    }
    
    @Override
    public boolean nextLine() {
        if (prefix.isEmpty()) {
            return suffix.nextLine();
        } else {
            boolean seenEOL = prefix.nextLine();
            if (seenEOL) {
                if (prefix.isEmpty()) suffix.frontTrim();
                return true;
            } else {
                return suffix.nextLine();
            }
        }
    }

    boolean frontTrim() {
        return prefix.isEmpty() ? suffix.frontTrim() : prefix.frontTrim();
    }

    @Override
    public Field nextField(char c) {
        if (prefix.isEmpty()) return suffix.nextField(c);//TODO: state machine ~ 5%
        int startIndex = currentIndex();
        int endIndex = currentIndex();
        while(hasMoreData()) {
            byte cur = currentByte();
            if (cur == c || cur == CR || cur == LF) {
                endIndex = currentIndex();
                if (cur == c) 
                    nextByte();
                break;
            } else {
                nextByte();
            }
        }
        if (currentIndex() == startIndex) return null;
        if (!hasMoreData()) endIndex = prefix.end + suffix.end;
        return createField(startIndex, endIndex, null);
    }
    
    @Override
    public Field nextField(char c, char quote) {
        if (prefix.isEmpty()) return suffix.nextField(c, quote);
        boolean inQuote = hasMoreData() && currentByte() == quote;
        if (!inQuote) return nextField(c);
        nextByte();
        int startIndex = currentIndex();
        int endIndex = currentIndex();
        while(hasMoreData()) {
            byte cur = currentByte();
            if (cur == c && prevByte() == quote) {
                endIndex = currentIndex() - 1;
                nextByte();
                break;
            } else {
                nextByte();
            }
        }
        if (currentIndex() == startIndex) return null;
        if (isEmpty()) {
            if (prevByte() == quote) endIndex = currentIndex() - 1; else endIndex = currentIndex();
        }
        return createField(startIndex, endIndex, quote);
    }

    private Field createField(int startIndex, int endIndex, Character quote) {
        if (startIndex >= prefix.end) {
            suffixFieldTemplateObject.modifyBounds(startIndex - prefix.end, endIndex - prefix.end, quote);
            return suffixFieldTemplateObject;
        }
        if (endIndex < prefix.end) {
            prefixFieldTemplateObject.modifyBounds(startIndex, endIndex, quote);
            return prefixFieldTemplateObject;
        }
        byte[] result = new byte[endIndex - startIndex];
        System.arraycopy(prefix.buffer, startIndex, result, 0, prefix.end - startIndex);
        System.arraycopy(suffix.buffer, 0, result, prefix.end - startIndex, endIndex - prefix.end);
        return new ByteArrayField(result, 0, result.length, quote);
    }

    @Override
    public String toString() {
        return new StringBuffer().append(prefix).append(suffix).toString();
    }
    
    byte prevByte() {
        if (suffix.currentIndex > suffix.start) return suffix.buffer[suffix.currentIndex - 1];
        return prefix.buffer[prefix.currentIndex - 1];
    }

    
    byte currentByte() {
        return prefix.isEmpty() ? suffix.buffer[suffix.currentIndex] : prefix.buffer[prefix.currentIndex];
    }

    void nextByte() {
        if (prefix.isEmpty()) suffix.currentIndex++; else prefix.currentIndex++;
    }
    
    int currentIndex() {
        return prefix.currentIndex + suffix.currentIndex; 
    }

}