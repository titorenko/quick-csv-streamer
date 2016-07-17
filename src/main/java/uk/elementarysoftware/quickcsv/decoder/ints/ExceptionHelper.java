package uk.elementarysoftware.quickcsv.decoder.ints;

class ExceptionHelper {
    static NumberFormatException nfExceptionFor(byte[] in, int startIndex, int len) {
        return new NumberFormatException("For: "+new String(in, startIndex, len));
    } 
}
