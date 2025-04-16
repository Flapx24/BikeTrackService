package com.example.demo.models;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GeoPoint {
    @NotNull(message = "Latitude is required")
    @DecimalMin(value = "-90.0", message = "Latitude must be greater than or equal to -90")
    @DecimalMax(value = "90.0", message = "Latitude must be less than or equal to 90")
    private Double lat;
    
    @NotNull(message = "Longitude is required")
    @DecimalMin(value = "-180.0", message = "Longitude must be greater than or equal to -180")
    @DecimalMax(value = "180.0", message = "Longitude must be less than or equal to 180")
    private Double lng;

    public GeoPoint() {
    }

    @JsonCreator
    public GeoPoint(@JsonProperty("lat") Double lat, @JsonProperty("lng") Double lng) {
        this.lat = lat;
        this.lng = lng;
    }
    
    /**
     * Creates a GeoPoint from a String with format "lat,lng"
     * 
     * @param coordString String in format "lat,lng"
     * @return GeoPoint
     * @throws IllegalArgumentException if the format is invalid
     */
    public static GeoPoint fromString(String coordString) {
        if (coordString == null || !coordString.contains(",")) {
            throw new IllegalArgumentException("Invalid coordinate format. Expected 'lat,lng'");
        }
        
        String[] parts = coordString.split(",");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid coordinate format. Expected 'lat,lng'");
        }
        
        try {
            Double lat = Double.parseDouble(parts[0].trim());
            Double lng = Double.parseDouble(parts[1].trim());
            return new GeoPoint(lat, lng);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid coordinate values. Latitude and longitude must be numeric");
        }
    }
    
    /**
     * Converts this GeoPoint to a String in format "lat,lng"
     * 
     * @return String representing the coordinates
     */
    @Override
    public String toString() {
        return lat + "," + lng;
    }
    
    public Double getLat() {
        return lat;
    }
    
    public void setLat(Double lat) {
        this.lat = lat;
    }
    
    public Double getLng() {
        return lng;
    }
    
    public void setLng(Double lng) {
        this.lng = lng;
    }
}