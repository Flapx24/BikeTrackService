package com.example.demo.services;

import java.util.List;

import com.example.demo.entities.Route;

public interface RouteService {
    
    Route saveRoute(Route route);
    
    Route findById(Long id);
    
    List<Route> getAllRoutes(Long lastRouteId);
    
    List<Route> getRoutesByCityAndMinScore(String city, Integer minScore, Long lastRouteId);
    
    boolean deleteRoute(Long id);
    
    /**
     * Normalizes a city name for searching
     * Converts to lowercase and removes accents
     * 
     * @param city City name to normalize
     * @return Normalized city name
     */
    String normalizeCity(String city);
}