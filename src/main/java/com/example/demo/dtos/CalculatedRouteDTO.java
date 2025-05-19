package com.example.demo.dtos;

import java.util.ArrayList;
import java.util.List;

import com.example.demo.models.GeoPoint;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CalculatedRouteDTO {

    private List<GeoPoint> routePoints;    private Integer estimatedTimeMinutes;
    private Double totalDistanceKm;
    private String vehicleType;
    private boolean success;
    private String message;

    public CalculatedRouteDTO() {
        this.success = false;
    }

    /**
     * Constructor for successfully calculated routes
     *
     * @param routePoints          List of geographic points that make up the route
     * @param estimatedTimeMinutes Estimated time in minutes to travel the route
     * @param totalDistanceKm      Total distance in kilometers
     * @param vehicleType          Vehicle type (BICYCLE, CAR, WALKING)
     */    public CalculatedRouteDTO(List<GeoPoint> routePoints, Integer estimatedTimeMinutes,
            Double totalDistanceKm, String vehicleType) {
        this.routePoints = routePoints;
        this.estimatedTimeMinutes = estimatedTimeMinutes;
        this.totalDistanceKm = totalDistanceKm;
        this.vehicleType = vehicleType;
        this.success = true;
        this.message = "Ruta calculada con éxito";
    }

    /**
     * Constructor for errors
     *
     * @param errorMessage Error message
     */
    public CalculatedRouteDTO(String errorMessage) {
        this.success = false;
        this.message = errorMessage;
    }

    // Getters and setters
    public List<GeoPoint> getRoutePoints() {
        return routePoints != null ? routePoints : new ArrayList<>();
    }

    public void setRoutePoints(List<GeoPoint> routePoints) {
        this.routePoints = routePoints;
    }    public Integer getEstimatedTimeMinutes() {
        return estimatedTimeMinutes;
    }

    public void setEstimatedTimeMinutes(Integer estimatedTimeMinutes) {
        this.estimatedTimeMinutes = estimatedTimeMinutes;
    }

    public Double getTotalDistanceKm() {
        return totalDistanceKm;
    }

    public void setTotalDistanceKm(Double totalDistanceKm) {
        this.totalDistanceKm = totalDistanceKm;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}