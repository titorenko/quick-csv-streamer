package uk.elementarysoftware.quickcsv.decoder.doubles;


class QuickDoubleParser implements DoubleParser {

	private static final int RADIX = 10;
	private static final int DOT = '.'-'0';
	
	private JDKDoubleParserAdapter fallBack = new JDKDoubleParserAdapter();
	
	public double parse(byte[] bytes, int offset, int length) {
		if (bytes == null || length <=0)
			throw new NumberFormatException("Empty input");
		long result = 0;
        boolean isNegative = false;
        int index = offset, dotIndex=offset+length-1, endIndex = offset+length;

        byte firstByte = bytes[index];
        if (firstByte < '0') {
            if (firstByte == '-') {
            	isNegative = true;
            }
            index++;
        }
        int nDigits = 0;
        while (index < endIndex) {
            int digit = bytes[index] - '0';
            if (digit == DOT) {
            	dotIndex=index;
            }else  if (digit < 0 || digit>9) {
                throw new NumberFormatException("For: "+new String(bytes, offset, length));
            } else {
           	  	result *= RADIX;
  	            result -= digit;
  	            nDigits++; 
            }
            index++;
        }
        
        double mantissa = -result;
        int negExponent = length-(dotIndex-offset)-1;
        
    	if (nDigits <= JDKDoubleParser.maxDecimalDigits) {
			if (negExponent == 0 || mantissa == 0.0) {
				return (isNegative) ? -mantissa : mantissa;
			}  
			double rValue = mantissa / JDKDoubleParser.small10pow[negExponent];
			return (isNegative) ? -rValue : rValue;
		} else { //harder case, use JDK implementation
			return fallBack.parse(bytes, offset, length);
		}
	}
}