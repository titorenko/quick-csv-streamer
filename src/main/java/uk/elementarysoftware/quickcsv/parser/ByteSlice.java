package uk.elementarysoftware.quickcsv.parser;

import java.nio.charset.Charset;

import uk.elementarysoftware.quickcsv.api.ByteArraySource.ByteArrayChunk;
import uk.elementarysoftware.quickcsv.api.CSVParserBuilder.CSVFileMetadata;
import uk.elementarysoftware.quickcsv.functional.Pair;
import uk.elementarysoftware.quickcsv.functional.PrimitiveFunctions.FunBiCharToBoolean;
import uk.elementarysoftware.quickcsv.functional.PrimitiveFunctions.FunBiCharToT;
import uk.elementarysoftware.quickcsv.functional.PrimitiveFunctions.FunCharToBoolean;
import uk.elementarysoftware.quickcsv.functional.PrimitiveFunctions.FunCharToT;


public interface ByteSlice {
    static final byte CR = 0xD;
    static final byte LF = 0xA;
    
    public static ByteSlice wrap(ByteArrayChunk it, Charset charset) {
        return new SingleByteSlice(it, charset);
    }
    
    public static ByteSlice empty() {
        return wrap(ByteArrayChunk.EMPTY, null);
    }

    public static ByteSlice join(ByteSlice prefix, ByteSlice suffix) {
        return new CompositeByteSlice((SingleByteSlice) prefix, (SingleByteSlice) suffix);
    }
    
    public Pair<ByteSlice, ByteSlice> splitOnLastLineEnd();
    
    public boolean nextLine();
    
    /**
     * Skip until next occurrence of c character. False if not found and end of slice is reached
     * @param c - character on which to break
     * @return true if character was actually found, false if end of slice reached
     */
    public boolean skipUntil(final char c);
    
    public boolean skipUntil(final char c, final char quote);
    
    /**
     * Returns next field and advances to next field. Returns null when end of line or end of slice is reached.
     * @param c - character that indicates field boundary
     * @return object to access field content 
     */
    public ByteArrayField nextField(final char c);
    
    public ByteArrayField nextField(final char c, final char quote);

    public int size();
    
    public boolean hasMoreData();

    default public boolean isEmpty() { 
        return !hasMoreData();
    }

    /**
     * String representation of current line. Mainly for debug purposes, can return broken line when in composite slice.
     * @return current line
     */
    public String currentLine();
    
    default public void skipField(final CSVFileMetadata metadata) {
        if (metadata.quote.isPresent()) 
            skipUntil(metadata.separator, metadata.quote.get());
        else
            skipUntil(metadata.separator);
    }
    
    default public ByteArrayField getNextField(final CSVFileMetadata metadata) {
        if (metadata.quote.isPresent()) 
            return nextField(metadata.separator, metadata.quote.get());
        else
            return nextField(metadata.separator);
    }

    public void incrementUse();
    
    public void decremenentUse();

}

class SingleByteSlice implements ByteSlice {
    final int start;//inclusive
    final int end;//exclusive
    final byte[] buffer;
    final ByteArrayField fieldTemplateObject;
    final Charset charset;
    final ByteArrayChunk src;
    
    int currentIndex;
    
    public SingleByteSlice(ByteArrayChunk src, Charset charset) {
        this(src, src.getData(), 0, src.getLength(), charset);
    }
    
    public SingleByteSlice(ByteArrayChunk src, byte[] buffer, int start, int end, Charset charset) {
        this.src = src;
        this.buffer = buffer;
        this.start = start;
        this.end = end;
        this.fieldTemplateObject = new ByteArrayField(buffer, 0, 0, charset);
        this.currentIndex = start;
        this.charset = charset;
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
    
    public String currentLine() {
        int startIdx = currentIndex;
        for(; startIdx > start && buffer[startIdx]!=CR && buffer[startIdx]!=LF; startIdx--);
        int endIdx = currentIndex;
        for(; endIdx < end && buffer[endIdx]!=CR && buffer[endIdx]!=LF; endIdx++);
        return new String(buffer, startIdx, endIdx - startIdx);
    }
    
    public Pair<ByteSlice, ByteSlice> splitOnLastLineEnd() {
        int i = end-1;
        for (;i >=currentIndex && buffer[i] != LF; i--);
        SingleByteSlice prefix = new SingleByteSlice(src, buffer, currentIndex, i+1, charset);
        SingleByteSlice suffix = new SingleByteSlice(src, buffer, i+1, end, charset);
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

    public ByteArrayField nextField(final char c) {
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
    public ByteArrayField nextField(char c, char q) {
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

    @Override
    public void incrementUse() {
        src.incrementUseCount();
    }

    @Override
    public void decremenentUse() {
        src.decrementUseCount();
    }
}

class CompositeByteSlice implements ByteSlice {

    private final SingleByteSlice prefix;
    private final SingleByteSlice suffix;
    private final ByteArrayField prefixFieldTemplateObject;
    private final ByteArrayField suffixFieldTemplateObject;
    
    private FunCharToT<ByteArrayField> nextFieldFun;
    private FunBiCharToT<ByteArrayField> nextFieldFunQuoted;
    private FunCharToBoolean skipUntilFun;
    private FunBiCharToBoolean skipUntilFunQuoted;

    CompositeByteSlice(SingleByteSlice prefix, SingleByteSlice suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
        this.prefixFieldTemplateObject = new ByteArrayField(prefix.buffer, 0, 0, prefix.charset);
        this.suffixFieldTemplateObject = new ByteArrayField(suffix.buffer, 0, 0, suffix.charset);
        
        this.nextFieldFun =  this::nextFieldWithPrefix;
        this.nextFieldFunQuoted =  this::nextFieldWithPrefix;
        this.skipUntilFun = this::skipUntilWithPrefix;
        this.skipUntilFunQuoted = this::skipUntilWithPrefix;
    }

    /*
     * -----------------------------------------------------------
     * Generic functions below work on slice with non-empty prefix, but once prefix has been
     * exhausted they will flip to simple suffix delegates. 
     * Only frequently called functions are implemented that way.
     * -----------------------------------------------------------
     */
    private ByteArrayField nextFieldWithPrefix(char c) {
        if (prefix.isEmpty()) {
            flip();
            return suffix.nextField(c);
        }
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
    
    private ByteArrayField nextFieldWithPrefix(char c, char quote) {
        if (prefix.isEmpty()) {
            flip();
            return suffix.nextField(c, quote);
        }
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
    
    private boolean skipUntilWithPrefix(char c) {
        if (prefix.isEmpty()) {
            flip();
            return suffix.skipUntil(c);
        }
        boolean isFound = prefix.skipUntil(c);
        if (isFound) {
            return true;
        } else {
            return suffix.skipUntil(c);
        }
    }
    
    private boolean skipUntilWithPrefix(char c, char q) {
        if (prefix.isEmpty()) {
            flip();
            return suffix.skipUntil(c, q);
        }
        boolean isFound = prefix.skipUntil(c, q);
        if (isFound) {
            return true;
        } else {
            return suffix.skipUntil(c, q);
        }
    }

    private void flip() {
        this.nextFieldFun =  suffix::nextField;
        this.nextFieldFunQuoted =  suffix::nextField;
        this.skipUntilFun = suffix::skipUntil;
        this.skipUntilFunQuoted = suffix::skipUntil;
    }
    /*
     * -----------------------------------------------------------
     * end
     * -----------------------------------------------------------
    */

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
    public ByteArrayField nextField(char c) {
        return nextFieldFun.apply(c);
    }
    
    @Override
    public ByteArrayField nextField(char c, char quote) {
        return nextFieldFunQuoted.apply(c, quote);
    }

    @Override
    public boolean skipUntil(char c) {
        return skipUntilFun.apply(c);
    }
    
    @Override
    public boolean skipUntil(char c, char q) {
        return skipUntilFunQuoted.apply(c, q);
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
    public String currentLine() {
        return prefix.isEmpty() ? suffix.currentLine() : prefix.currentLine();
    }

    private ByteArrayField createField(int startIndex, int endIndex, Character quote) {
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
        return new ByteArrayField(result, 0, result.length, prefix.charset, quote);
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

    @Override
    public void decremenentUse() {
        prefix.src.decrementUseCount();
        suffix.src.decrementUseCount();
    }

    @Override
    public void incrementUse() {
        throw new IllegalStateException("Should not be called");
    }
}