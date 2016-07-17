package uk.elementarysoftware.quickcsv.parser;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import uk.elementarysoftware.quickcsv.api.ByteArraySource.ByteArrayChunk;
import uk.elementarysoftware.quickcsv.api.Field;
import uk.elementarysoftware.quickcsv.functional.Pair;

public class ByteSliceTest {
    
    private static final String FIELDS22 = "field11,field12\nfield21,field22";
    private static final String FIELDS33 = "field11,field12,field13\nfield21,field22,field23\nfield31,field32,field33";
    
    @Test
    public void testSplitOnLastLineEnd() {
        String content = "line1\nline2\nlastline";
        ByteSlice slice = sliceFor(content.getBytes());
        assertEquals(content, slice.toString());
        Pair<ByteSlice, ByteSlice> sliced = slice.splitOnLastLineEnd();
        assertEquals("line1\nline2\n", sliced.first.toString());
        assertEquals("lastline", sliced.second.toString());
    }
    
    
    @Test
    public void testSplitOnLastLineEndWithSkip() {
        String content = "line1\nline2\nlastline";
        ByteSlice slice = sliceFor(content.getBytes());
        slice.nextLine();
        Pair<ByteSlice, ByteSlice> sliced = slice.splitOnLastLineEnd();
        assertEquals("line2\n", sliced.first.toString());
        assertEquals("lastline", sliced.second.toString());
    }
    
    @Test
    public void testSingleSlice() {
        ByteSlice slice = sliceFor(FIELDS22.getBytes());
        assertEquals("field11,field12", slice.currentLine());
        List<Field> fields = getFields(slice);
        assertArrayEquals(new String[] {"field11","field12","field21","field22"}, fields.stream().map(f -> f.asString()).toArray());
    }
    
    @Test
    public void testSingleSliceFieldSplitWithQuote() {
        ByteSlice slice = sliceFor("f1,\"f2,f2\",f3,\"f\"\"4\"".getBytes());
        assertEquals("f1", slice.nextField(',', '"').asString());
        assertEquals("f2,f2", slice.nextField(',', '"').asString());
        assertEquals("f3", slice.nextField(',', '"').asString());
        assertEquals("f\"4", slice.nextField(',', '"').asString());
    }
    
    @Test
    public void testMultiSliceQuoteSplit() {
        String content = "f1,\"f2,f2\",f3,\"f\"\"4\"";
        for (int splitIndex = 0; splitIndex < content.length(); splitIndex++) {
            String prefix = content.substring(0, splitIndex);
            String suffix = content.substring(splitIndex);
            ByteSlice join = ByteSlice.join(sliceFor(prefix.getBytes()), sliceFor(suffix.getBytes()));
            assertEquals(content, join.toString());
            List<Field> fields = getFieldsQuoted(join);
            assertArrayEquals(
                    "Failed on split index "+splitIndex,
                    new String[] {"f1","f2,f2","f3","f\"4"}, 
                    fields.stream().map(f -> f.asString()).toArray());
        }
    }
    
    @Test
    public void testEmptyFieldHandling() {
        ByteSlice slice = sliceFor("f1,,f2".getBytes());
        assertEquals("f1", slice.nextField(',', '"').asString());
        assertEquals("", slice.nextField(',', '"').asString());
        assertEquals("f2", slice.nextField(',', '"').asString());
        assertNull(slice.nextField(',', '"'));
    }
    
    @Test
    public void testSkipSlice() {
        ByteSlice slice = sliceFor(FIELDS22.getBytes());
        slice.skipUntil(',');
        assertEquals("field12", slice.nextField(',').asString());
    }
    
    @Test
    public void testSkipSliceQuoted() {
        ByteSlice slice = sliceFor("f1,\"f2,f2\",f3".getBytes());
        slice.skipUntil(',', '"');
        slice.skipUntil(',', '"');
        assertEquals("f3", slice.nextField(',', '"').asString());
    }
    
    
    @Test
    public void testMultiSliceIteration() {
        String content = FIELDS22;
        int splitIndex = 3;
        String prefix = content.substring(0, splitIndex);
        String suffix = content.substring(splitIndex);
        CompositeByteSlice slice = (CompositeByteSlice) ByteSlice.join(sliceFor(prefix.getBytes()), sliceFor(suffix.getBytes()));
        byte[] result = new byte[slice.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = slice.currentByte();
            slice.nextByte();
        }
        assertEquals(FIELDS22, new String(result));
    }
    
    @Test
    public void testMultiSliceFieldSplit() {
        String content = FIELDS33;
        for (int splitIndex = 0; splitIndex < content.length(); splitIndex++) {
            String prefix = content.substring(0, splitIndex);
            String suffix = content.substring(splitIndex);
            ByteSlice join = ByteSlice.join(sliceFor(prefix.getBytes()), sliceFor(suffix.getBytes()));
            assertEquals(content, join.toString());
            List<Field> fields = getFields(join);
            assertArrayEquals(
                    "Failed on split index "+splitIndex,
                    new String[] {"field11","field12","field13","field21","field22","field23","field31","field32","field33"}, 
                    fields.stream().map(f -> f.asString()).toArray());
        }
    }
    
    @Test
    public void testMultiSliceSkip() {
        String content = FIELDS33;
        for (int splitIndex = 0; splitIndex < content.length(); splitIndex++) {
            String prefix = content.substring(0, splitIndex);
            String suffix = content.substring(splitIndex);
            ByteSlice join = ByteSlice.join(sliceFor(prefix.getBytes()), sliceFor(suffix.getBytes()));
            assertTrue(join.skipUntil(','));
            assertEquals("field12", join.nextField(',').asString());
            assertTrue(join.nextLine());
            assertEquals("field21", join.nextField(',').asString());
            assertTrue(join.skipUntil(','));
            assertEquals("field23", join.nextField(',').asString());
        }
    }
    
    private ByteSlice sliceFor(byte[] bytes) {
        return ByteSlice.wrap(new ByteArrayChunk(bytes, bytes.length, false, (b) -> {}), Charset.defaultCharset());
    }

    private List<Field> getFields(ByteSlice bs) {
        List<Field> result = new ArrayList<>();
        while(true) {
            ByteArrayField f = bs.nextField(',');
            if (f == null) {
                if (!bs.nextLine()) break;
            } else {
                result.add(f.clone());
            }
        }
        return result;
    }
    
    private List<Field> getFieldsQuoted(ByteSlice bs) {
        List<Field> result = new ArrayList<>();
        while(true) {
            ByteArrayField f = bs.nextField(',', '"');
            if (f == null) break;
            result.add(f.clone());
        }
        return result;
    }
}
