package uk.elementarysoftware.quickcsv.benchmarks;

import java.util.function.Function;

import uk.elementarysoftware.quickcsv.api.CSVRecord;

public class City {
    
    public static final Function<CSVRecord, City> MAPPER = (CSVRecord r) -> new City(r);
    
    private static final int CITY_INDEX = 2;
    
    private final String city;
    private final int population; 
    private final double latitude;
    private final double longitude;
    
    public City(CSVRecord r) {
        r.skipFields(CITY_INDEX);
        this.city  = r.getNextField().asString();
        r.skipField();
        this.population = r.getNextField().asInt();
        this.latitude = r.getNextField().asDouble();
        this.longitude = r.getNextField().asDouble();
    }
    
    public City(String city, int population, double latitude, double longitude) {
        this.city = city;
        this.population = population;
        this.latitude = latitude;
        this.longitude = longitude;
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
