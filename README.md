Quick CSV Streamer  [![Build Status](https://travis-ci.org/titorenko/quick-csv-streamer.svg?branch=master)](https://travis-ci.org/titorenko/quick-csv-streamer)
=============

Quick CSV streamer is CSV parsing library with Java 8 Stream API with particular focus on performance.
Additionally the library provides fast mappers to standard Java types, like doubles and ints.
Parallel, multi-core parsing is supported as well as single-core sequential parsing using standard
stream API. Usually you would be able to achieve 2x speed up on single core, compared to other
Java CSV parsing libraries and even more on multiple core or if your input file is sparse.

Maven dependency
--------------

Available from Maven Central:

    <dependency>
        <groupId>uk.elementarysoftware</groupId>
        <artifactId>quick-csv-streamer</artifactId>
        <version>0.1.1</version>
    </dependency>

Example usage
--------------

Suppose following CSV file needs to be parsed
    
    Country,City,AccentCity,Region,Population,Latitude,Longitude
    ad,andorra,Andorra,07,,42.5,1.5166667
    gb,city of london,City of London,H9,,51.514125,-.093689
    ua,kharkiv,Kharkiv,07,,49.980814,36.252718
    
First define Java class to represent the records as follows 

    public class City {
        private final String city;
        private final int population; 
        private final double latitude;
        private final double longitude;
        
        ...
    }
   
To parse the file 
    
    import uk.elementarysoftware.quickcsv.api.*;
    
    Stream<CSVRecord> records = CSVParserBuilder.aParser().forRfc4180().skipFirstRecord()
    	.build().parse(new File("cities.txt"));

Then to convert records 
       
    Stream<City> cities = records.map(r -> new City(r));
    
    public City(CSVRecord r) {
        r.skipFields(2);
        this.city  = r.getNextField().asString();
        r.skipField();
        this.population = r.getNextField().asInt();
        this.latitude = r.getNextField().asDouble();
        this.longitude = r.getNextField().asDouble();
    }    
    
Note that CSVRecord has pull-style API that allows for targeted selection of attributes, with unused attributes ignored in most efficient manner.

After that stream can be consumed in parallel or on single thread using normal Java stream API.


Performance
--------------    

Best way to check performance of the library is to run benchmark on your local system with

    gradle bench
    
It is very important to appreciate that performance might differ drastically depending on the actual CSV content. As very rough guideline below is sample output of "gradle bench" on i7 2700k Ubuntu system, which uses cities.txt similar to example above, expanded to have 3173800 rows and 157 MB in size:

|Benchmark            |Mode  |Cnt  |   Score |   Error  |Units|
| ------------------- | ---- | --- | ------- | -------- | --- | 
|OpenCSVParser        |avgt  |  5  |2613.354 |± 53.583  |ms/op|
|Quick CSV Parallel   |avgt  |  5  | 190.009 |±  9.800  |ms/op|
|Quick CSV Sequential |avgt  |  5  | 698.985 |± 50.478  |ms/op|


Comparison is done with OpenCSV library, performance of other libraries can be extrapolated using chart from https://github.com/uniVocity/csv-parsers-comparison 

Prerequisites
--------------
Quick CSV Streamer library requires Java 8, it has no other dependencies and thus very lightweight.

License
--------------
Library is licensed under the terms of GPL v.2 license.
        
