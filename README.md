Quick CSV Streamer  
=============

[![Build Status](https://travis-ci.org/titorenko/quick-csv-streamer.svg?branch=master)](https://travis-ci.org/titorenko/quick-csv-streamer)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/uk.elementarysoftware/quick-csv-streamer/badge.svg)](https://maven-badges.herokuapp.com/maven-central/uk.elementarysoftware/quick-csv-streamer/)

Quick CSV streamer is a high performance CSV parsing library with Java 8 Stream API.
The library omits many redundant steps found in other open source parsers and produces minimal amount
of garbage during parsing, reducing pressure on the garbage collector.
Parallel, multi-core parsing is supported transparently via Java Stream API.

Compared to other open source Java CSV parsing libraries likely speeds up will be at 2x - 10x range in sequential mode. Naturally parallel mode will improve performance even further. See benchmarking results below for more details.


Maven dependency
--------------

Available from Maven Central:

```xml
<dependency>
    <groupId>uk.elementarysoftware</groupId>
    <artifactId>quick-csv-streamer</artifactId>
    <version>0.2.0</version>
</dependency>
```

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

here we will be sourcing 4 fields from the source file, ignoring other 3.  

Parsing the file is simple

    import uk.elementarysoftware.quickcsv.api.*;

    CSVParser<City> parser = CSVParserBuilder.aParser(City::new, City.CSVFields.class).forRfc4180().build();

the parser will be using CSV separators as per RFC 4180, default encoding and will be expecting header as first record in the source. Custom separators, quotes, encodings and header sources are supported.

Actual mapping is done in `City` constructor

    public class City {

        public static enum CSVFields {
            AccentCity,
            Population,
            Latitude,
            Longitude
        }

        public City(CSVRecordWithHeader<CSVFields> r) {
            this.city = r.getField(CSVFields.AccentCity).asString();
            this.population = r.getField(CSVFields.Population).asInt();
            this.latitude = r.getField(CSVFields.Latitude).asDouble();
            this.longitude = r.getField(CSVFields.Longitude).asDouble();
        }

first `CSVFields` enum specifies which fields should be sourced and only these fields will be actually parsed. After that `CSVRecordWithHeader` instance is used to populate `City` instance fields, refering to CSV fields by enum values.

Of course mapping can also be done outside domain class constructor, just pass different `Function<CSVRecordWithHeader, City>` to `CSVParserBuilder`.

Resulting stream can be processed in parallel or sequentially with usual Java stream API. For example to parse sequentially on  a single thread

    Stream<City> stream = parser.parse(source).sequential();
    stream.forEach(System.out::println);    

By default parser will operate in parallel mode.

Please see [sample project](https://github.com/titorenko/quick-csv-streamer-cities-sample) for full source code of the above example.

Special cases for headers
--------------

When header contains special characters the fields can not be simply encoded by enum literals. In such cases `toString` should be overwritten, for example

    enum Fields {
        Latitude("City Latitude"),
        Longitude("City Longitude"),
        City("City name"),
        Population("City Population");

        private final String headerFieldName;

        private Fields(String headerFieldName) {
            this.headerFieldName = headerFieldName;
        }

        @Override public String toString() {
            return headerFieldName;
        }
    }

If header is missing from the source it can be supplied during parser constuction

    CSVParserBuilder
        .aParser(City::new, City.CSVFields.class)
        .usingExplicitHeader("Country", "City", "AccentCity", "Region", "Population", "Latitude", "Longitude")
        .build();


Advanced usage
--------------
About 10% performance improvement compared to normal usage can be achieved by referencing the fields by position instead of name. In this case parser construction is even simpler

    CSVParser<City> parser = CSVParserBuilder.aParser(City::new).build();

as enumeration specifying field names is not needed. However now constructor will be using `CSVRecord` interface  

    public City(CSVRecord r) {
        r.skipFields(2);
        this.city  = r.getNextField().asString();
        r.skipField();        
        this.population = r.getNextField().asInt();        
        this.latitude = r.getNextField().asDouble();
        this.longitude = r.getNextField().asDouble();
    }

effectively this encodes field order in the CSV source.

Performance
--------------    

Best way to check performance of the library is to run benchmark on your target system with

    gradle jmh

reports can be then found in build/reports/jmh.

It is very important to appreciate that performance might vary dramattically depending on the actual CSV content. As a very rough guideline see below sample output of "gradle jmh" on i7 2700k Ubuntu system, which uses `cities.txt` similar to example above, expanded to have 3173800 rows and 157 MB in size:

|Benchmark                      |Mode  |Cnt  |   Score |   Error   |Units|
| ----------------------------- | ---- | --- | ------- | --------- | --- |
|OpenCSVParser                  |avgt  |  5  |2405.238 |± 82.958   |ms/op|
|Quick CSV Parallel with header |avgt  |  5  | 205.132 |±  2.193   |ms/op|
|Quick CSV Parallel (advanced)  |avgt  |  5  | 178.582 |±  0.640   |ms/op|
|Quick CSV Sequential           |avgt  |  5  | 660.334 |± 67.605   |ms/op|


Comparison is done with OpenCSV library v3.8, performance of other libraries can be extrapolated using chart from https://github.com/uniVocity/csv-parsers-comparison

Prerequisites
--------------
Quick CSV Streamer library requires Java 8, it has no other dependencies.

License
--------------
Library is licensed under the terms of [GPL v2.0 license](http://www.gnu.org/licenses/gpl-2.0.html).
Please contact me if you wish to use this library under more commercially friendly license.
