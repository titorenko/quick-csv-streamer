package com.elementarysoftware.quickcsv.manual;

import java.io.File;
import java.net.URL;

import org.apache.commons.io.FileUtils;

import com.elementarysoftware.quickcsv.api.CSVParser;
import com.elementarysoftware.quickcsv.api.CSVParserBuilder;
import com.elementarysoftware.quickcsv.sampledomain.City;


public class CityManualPerformanceTester {
	
	public void run() throws Exception {
		File file = prepareFile(100);
		System.out.println("Running file of size "+(file.length() / 1024 / 1024)+ "MB");
		run(file, 1500);
		file.delete();
	}
	
	
	private void run(File source, int nRuns) throws Exception {
	    CSVParser parser = new CSVParserBuilder().build();
		long maxSpeed = 0;
		for (int i = 0; i < nRuns; i++) {
			long start = System.currentTimeMillis();
			parser.parse(source).map(City.MAPPER).count();
			long duration = System.currentTimeMillis() - start;
			if (duration == 0) continue;
			System.out.println("done " +source.getName()+" in "+duration);
			long speed = source.length()/1024/duration;
			if (speed > maxSpeed) maxSpeed = speed;  
			System.out.println("speed: "+(source.length()/1024/duration)+" MB/s, max: "+maxSpeed);
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
