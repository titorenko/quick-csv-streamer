package uk.elementarysoftware.quickcsv.sampledomain;

import java.util.function.Function;

import uk.elementarysoftware.quickcsv.api.CSVRecordWithHeader;

public class City2 {
    
	public static enum Fields {
		AccentCity,
        Latitude,
        Longitude,
        Population
    }
	
    public static final Function<CSVRecordWithHeader<Fields>, City2> MAPPER = City2::new;
    
    private final String city;
    private final int population; 
    private final double latitude;
    private final double longitude;
    
    public City2(CSVRecordWithHeader<Fields> r) {
        this.city  = r.getField(Fields.AccentCity).asString();
        this.population = r.getField(Fields.Population).asInt();
        this.latitude = r.getField(Fields.Latitude).asDouble();
        this.longitude = r.getField(Fields.Longitude).asDouble();
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
        City2 other = (City2) obj;
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
