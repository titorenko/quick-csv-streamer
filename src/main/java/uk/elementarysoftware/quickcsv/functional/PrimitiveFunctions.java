package uk.elementarysoftware.quickcsv.functional;

public class PrimitiveFunctions {
    
    @FunctionalInterface
    public static interface FunCharToT<T> {
        public T apply(char c);
    }
    
    @FunctionalInterface
    public static interface FunBiCharToT<T> {
        public T apply(char c, char q);
    }
    
    @FunctionalInterface
    public static interface FunCharToBoolean {
        public boolean apply(char c);
    }
    
    @FunctionalInterface
    public static interface FunBiCharToBoolean {
        public boolean apply(char c, char q);
    }
}