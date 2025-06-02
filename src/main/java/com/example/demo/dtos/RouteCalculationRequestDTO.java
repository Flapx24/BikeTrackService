package com.example.demo.dtos;

import java.util.List;

import com.example.demo.enums.VehicleType;
import com.example.demo.models.GeoPoint;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RouteCalculationRequestDTO {
    
    @NotNull(message = "Los puntos de la ruta son obligatorios")
    @NotEmpty(message = "Se requiere al menos dos puntos para calcular la ruta")
    @Size(min = 2, max = 50, message = "Se requieren entre 2 y 50 puntos para calcular una ruta")
    @Valid
    private List<GeoPoint> points;
    
    @NotNull(message = "El tipo de vehículo es obligatorio")
    @JsonProperty("vehicleType")
    private VehicleType vehicleType;

    public RouteCalculationRequestDTO() {
    }

    public RouteCalculationRequestDTO(List<GeoPoint> points, VehicleType vehicleType) {
        this.points = points;
        this.vehicleType = vehicleType;
    }

    public List<GeoPoint> getPoints() {
        return points;
    }

    public void setPoints(List<GeoPoint> points) {
        this.points = points;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getVehicleTypeAsString() {
        return vehicleType != null ? vehicleType.name() : null;
    }
}
