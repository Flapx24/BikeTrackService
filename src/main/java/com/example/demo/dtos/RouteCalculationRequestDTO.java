package com.example.demo.dtos;

import java.util.List;
import com.example.demo.models.GeoPoint;

public class RouteCalculationRequestDTO {
    private List<GeoPoint> points;
    private String vehicleType;

    public RouteCalculationRequestDTO() {

    }

    public RouteCalculationRequestDTO(List<GeoPoint> points, String vehicleType) {
        this.points = points;
        this.vehicleType = vehicleType;
    }

    public List<GeoPoint> getPoints() {
        return points;
    }

    public void setPoints(List<GeoPoint> points) {
        this.points = points;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }
}
