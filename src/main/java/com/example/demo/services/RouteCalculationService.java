package com.example.demo.services;

import java.util.List;

import com.example.demo.dtos.CalculatedRouteDTO;
import com.example.demo.models.GeoPoint;

public interface RouteCalculationService {

    /**
     * Calculate a route based on the given points and vehicle type
     *
     * @param points      List of geographic points for the route
     * @param vehicleType Type of vehicle (BICYCLE, CAR, WALKING)
     * @return A DTO with the calculated route information
     */
    CalculatedRouteDTO calculateRoute(List<GeoPoint> points, String vehicleType);
}