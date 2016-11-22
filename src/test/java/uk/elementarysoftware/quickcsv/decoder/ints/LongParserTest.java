package uk.elementarysoftware.quickcsv.decoder.ints;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Function;

import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

@RunWith(Theories.class)
public class LongParserTest {
    
    private static final int randomSize = 1000; 
    
    private static final Random rnd = new Random();
    
    @DataPoints("validLongs")
    public static String[] randomLongs() {
        return rnd.ints(randomSize).mapToObj(i -> ""+i).toArray(String[]::new);
    }
    
    @DataPoints("validLongs")
    public static String[] specialLongs() {
        return new String[] {"0", "-0", "+0", "+1", Long.MAX_VALUE+"", Long.MIN_VALUE+""};
    }
    
    @DataPoints("failingLongs") 
    public static String[] specialFailingLongs() {
        return new String[] {"X0", "-", "+", Double.MAX_VALUE+"", "", "Hello"};
    }
    
    private QuickLongParser parser = new QuickLongParser();
    
    @Theory
    public void parsersAreEquivalentOnValidLongs(@FromDataPoints("validLongs") String intValue) {
        compareParsingResult(intValue, s -> Long.parseLong(s), s -> parser.parse(s));
    }
    
    @Theory
    public void parsersAreEquivalentOnFailingLongs(@FromDataPoints("failingLongs") String intValue) {
        compareParsingResult(intValue, s -> Long.parseLong(s), s -> parser.parse(s));
    }
    
    private void compareParsingResult(String value, Function<String, Long> p1, Function<String, Long> p2) {
        Object v1 = null;
        try {
            v1 = p1.apply(value);
        } catch (Exception e) {
            v1 = e;
        }
        Object v2 = null;
        try {
            v2 = p2.apply(value);
        } catch (Exception e) {
            v2 = e;
        }
        assertEquals("Value 2:"+v2+", value 1: "+v1+", source"+value+"; "+Arrays.toString(value.getBytes()), v2.getClass(), v1.getClass());
        
        if (v2 instanceof Long) {
            assertEquals(v2, v1);
        }
    }
    
    public static void main(String[] args) {
        byte[] x = new byte[] {-39, -94};
        long l = Long.parseLong(new String(x));
        System.out.println(l);
    }
}