package uk.elementarysoftware.quickcsv.manual;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import uk.elementarysoftware.quickcsv.api.CSVParser;
import uk.elementarysoftware.quickcsv.api.CSVParserBuilder;
import uk.elementarysoftware.quickcsv.sampledomain.City;


public class CityManualPerformanceTester {
    long maxSpeed = 0;
    
	public void run() throws Exception {
	    File file = prepareFile(300);
	    try {
    		System.out.println("Running file of size "+(file.length() / 1024 / 1024)+ "MB");
    		run(file, 30);
	    } finally {
	        file.delete();
        } 
	}
	
	private void run(File source, int nRuns) throws Exception {
	    CSVParser<City> parser = CSVParserBuilder.aParser(City.MAPPER).build();
	    //CSVParser<City> parser = CSVParserBuilder.aParser(City.HeaderAwareMapper.MAPPER, City.HeaderAwareMapper.Fields.class).usingExplicitHeader("Country", "City", "AccentCity", "Region", "Population", "Latitude", "Longitude").build();//TODO add that example to docs
		
	    for (int i = 0; i < nRuns; i++) {
			runOnce(parser, source);
		}
	}

	private void runOnce(CSVParser<City> parser, File source) throws IOException {
        long start = System.currentTimeMillis();
        parser.parse(source).count();
        long duration = System.currentTimeMillis() - start;
        if (duration == 0) return;
        System.out.println("P2 parsed " +source.getName()+" in "+duration);
        long speed = source.length()/1024/duration;
        if (speed > maxSpeed) maxSpeed = speed;  
        System.out.println("P2 speed: "+(source.length()/1024/duration)+" MB/s, max: "+maxSpeed);
        
    }

    private File prepareFile(int sizeMultiplier) throws Exception {
		InputStream is = getClass().getResourceAsStream("/cities-unix.txt");
		byte[] content = IOUtils.toByteArray(is);
		File result = File.createTempFile("csv", "large");
		for (int i = 0; i < sizeMultiplier; i++) {
			FileUtils.writeByteArrayToFile(result, content, true);
		}
		return result;
	}
	
	public static void main(String[] args) throws Exception {
		new CityManualPerformanceTester().run(); 
	}
}
