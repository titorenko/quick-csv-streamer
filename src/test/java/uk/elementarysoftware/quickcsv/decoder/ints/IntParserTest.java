package uk.elementarysoftware.quickcsv.decoder.ints;

import static org.junit.Assert.assertEquals;

import java.util.Random;
import java.util.function.Function;

import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

@RunWith(Theories.class)
public class IntParserTest {
    
    private static final int randomSize = 1000; 
    
    private static final Random rnd = new Random();
    
    @DataPoints("validInts")
    public static String[] randomInts() {
        return rnd.ints(randomSize).mapToObj(i -> ""+i).toArray(String[]::new);
    }
    
    @DataPoints("validInts")
    public static String[] specialInts() {
        return new String[] {"0", "-0", "+0", "+1", Integer.MAX_VALUE+"", Integer.MIN_VALUE+""};
    }
    
    @DataPoints("failingInts") 
    public static String[] specialFailingInts() {
        return new String[] {"X0", "-", "+", Long.MAX_VALUE+"", "", "Hello"};
    }
    
    private QuickIntParser parser = new QuickIntParser();
    
    @Theory
    public void parsersAreEquivalentOnValidInts(@FromDataPoints("validInts") String intValue) {
        compareParsingResult(intValue, s -> Integer.parseInt(s), s -> parser.parse(s));
    }
    
    @Theory
    public void parsersAreEquivalentOnFailingInts(@FromDataPoints("failingInts") String intValue) {
        compareParsingResult(intValue, s -> Integer.parseInt(s), s -> parser.parse(s));
    }
    
    private void compareParsingResult(String value, Function<String, Integer> p1, Function<String, Integer> p2) {
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
        assertEquals(v2.getClass(), v1.getClass());
        if (v2 instanceof Integer) {
            assertEquals(v2, v1);
        }
    }
}