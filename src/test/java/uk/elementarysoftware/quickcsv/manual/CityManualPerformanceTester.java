package uk.elementarysoftware.quickcsv.manual;

import java.io.File;
import java.net.URL;

import org.apache.commons.io.FileUtils;

import uk.elementarysoftware.quickcsv.api.CSVParser;
import uk.elementarysoftware.quickcsv.api.CSVParserBuilder;
import uk.elementarysoftware.quickcsv.sampledomain.City;
import uk.elementarysoftware.quickcsv.sampledomain.City2;


public class CityManualPerformanceTester {
	
	public void run() throws Exception {
		File file = prepareFile(100);
		System.out.println("Running file of size "+(file.length() / 1024 / 1024)+ "MB");
		run(file, 1500);
		file.delete();
	}
	
	private void run(File source, int nRuns) throws Exception {
	    CSVParser<City> parser1 = CSVParserBuilder.aParser(City.MAPPER).build();
	    CSVParser<City2> parser2 = CSVParserBuilder.aParser(City2.MAPPER, City2.Fields.class)
	    		.usingExplicitHeader("Country", "City", "AccentCity", "Region", "Population", "Latitude", "Longitude").build();
		long maxSpeed1 = 0;
		long maxSpeed2 = 0;
		for (int i = 0; i < nRuns; i++) {
			long start = System.currentTimeMillis();
			parser1.parse(source).count();
			long duration = System.currentTimeMillis() - start;
			if (duration == 0) continue;
			System.out.println("P1 parsed " +source.getName()+" in "+duration);
			long speed = source.length()/1024/duration;
			if (speed > maxSpeed1) maxSpeed1 = speed;  
			System.out.println("P1 speed: "+(source.length()/1024/duration)+" MB/s, max: "+maxSpeed1);
			
			start = System.currentTimeMillis();
			parser2.parse(source).count();
			duration = System.currentTimeMillis() - start;
			if (duration == 0) continue;
			System.out.println("P2 parsed " +source.getName()+" in "+duration);
			speed = source.length()/1024/duration;
			if (speed > maxSpeed2) maxSpeed2 = speed;  
			System.out.println("P2 speed: "+(source.length()/1024/duration)+" MB/s, max: "+maxSpeed2);
		}
	}

	private File prepareFile(int sizeMultiplier) throws Exception {
		URL fileUrl = getClass().getResource("/cities-unix.txt");
		byte[] content = FileUtils.readFileToByteArray(new File(fileUrl.toURI()));
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
