package com.example.demo.models;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class GeoPoint {
    @NotNull(message = "La latitud es obligatoria")
    @DecimalMin(value = "-90.0", message = "La latitud debe ser mayor o igual a -90")
    @DecimalMax(value = "90.0", message = "La latitud debe ser menor o igual a 90")
    private Double lat;
    
    @NotNull(message = "La longitud es obligatoria")
    @DecimalMin(value = "-180.0", message = "La longitud debe ser mayor o igual a -180")
    @DecimalMax(value = "180.0", message = "La longitud debe ser menor o igual a 180")
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
            throw new IllegalArgumentException("Formato de coordenadas inválido. Se esperaba 'lat,lng'");
        }
        
        String[] parts = coordString.split(",");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Formato de coordenadas inválido. Se esperaba 'lat,lng'");
        }
        
        try {
            Double lat = Double.parseDouble(parts[0].trim());
            Double lng = Double.parseDouble(parts[1].trim());
            return new GeoPoint(lat, lng);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Valores de coordenadas inválidos. La latitud y longitud deben ser numéricas");
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