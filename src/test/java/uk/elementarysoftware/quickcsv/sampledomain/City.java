package uk.elementarysoftware.quickcsv.sampledomain;

import java.util.function.Function;

import uk.elementarysoftware.quickcsv.api.CSVRecord;
import uk.elementarysoftware.quickcsv.api.CSVRecordWithHeader;
import uk.elementarysoftware.quickcsv.api.Field;

public class City {
    
    public static final Function<CSVRecord, City> MAPPER = City::new;
    
    public static class HeaderAwareMapper {
        
        public static enum Fields {
            AccentCity,
            Latitude,
            Longitude,
            Population
        }
        
        public static final Function<CSVRecordWithHeader<Fields>, City> MAPPER = r -> {
            return new City(
                r.getField(Fields.AccentCity).asString(),
                r.getField(Fields.Population).asInt(),
                r.getField(Fields.Latitude).asDouble(),
                r.getField(Fields.Longitude).asDouble(),
                r.getField(Fields.Population).asLong()
            );
        };
    }
    
    public static class HeaderAwareMapper2 {
        public static enum Fields {
            AccentCity, Population, Latitude, Longitude, Country, City
        }
        
        public static final Function<CSVRecordWithHeader<Fields>, City> MAPPER = r -> {
            return new City(
                r.getField(Fields.City).asString(),
                r.getField(Fields.Population).asInt(),
                r.getField(Fields.Latitude).asDouble(),
                r.getField(Fields.Longitude).asDouble(),
                r.getField(Fields.Population).asLong()
            );
        };
    }
    
    private static final int CITY_INDEX = 2;
    
    private final String city;
    private final int population; 
    private final double latitude;
    private final double longitude;
    private final long populationL;
    
    public City(CSVRecord r) {
        r.skipFields(CITY_INDEX);
        this.city  = r.getNextField().asString();
        r.skipField();
        Field popField = r.getNextField();
        this.population = popField.asInt();
        this.populationL = popField.asLong();
        this.latitude = r.getNextField().asDouble();
        this.longitude = r.getNextField().asDouble();
    }
    
    public City(String city, int population, double latitude, double longitude, long populationL) {
        this.city = city;
        this.population = population;
        this.latitude = latitude;
        this.longitude = longitude;
        this.populationL = populationL;
    }

    public String getCity() {
        return city;
    }
    
    public int getPopulation() {
        return population;
    }
    
    public double getLatitude() {
        return latitude;
    }
    
    public double getLongitude() {
        return longitude;
    }
    
    public long getPopulationL() {
        return populationL;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((city == null) ? 0 : city.hashCode());
        long temp;
        temp = Double.doubleToLongBits(latitude);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + population;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        City other = (City) obj;
        if (city == null) {
            if (other.city != null)
                return false;
        } else if (!city.equals(other.city))
            return false;
        if (Double.doubleToLongBits(latitude) != Double.doubleToLongBits(other.latitude))
            return false;
        if (Double.doubleToLongBits(longitude) != Double.doubleToLongBits(other.longitude))
            return false;
        if (population != other.population)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "City [city=" + city + ", population=" + population + ", latitude=" + latitude + ", longitude=" + longitude + "]";
    }
    
}
