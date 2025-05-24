package com.example.demo.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.demo.dtos.RouteDTO;
import com.example.demo.entities.Route;

public interface RouteService {

    Route saveRoute(Route route);

    Route findById(Long id);

    List<Route> getAllRoutes(Long lastRouteId);

    List<Route> getRoutesByCityAndMinScore(String city, Integer minScore, Long lastRouteId);

    boolean deleteRoute(Long id);

    /**
     * Gets routes filtered by city, title and ordered by popularity
     * 
     * @param city  Optional filter by city
     * @param title Optional filter by title
     * @param sort  Sort direction: 'asc', 'desc' or 'none'
     * @return List of filtered and ordered routes
     */
    List<RouteDTO> getFilteredRoutes(String city, String title, String sort);

    /**
     * Gets routes filtered by city, title and ordered by popularity with pagination
     * 
     * @param city     Optional filter by city
     * @param title    Optional filter by title
     * @param sort     Sort direction: 'asc', 'desc' or 'none'
     * @param pageable Pagination information
     * @return Page of filtered and ordered routes
     */
    Page<RouteDTO> getFilteredRoutesPaginated(String city, String title, String sort, Pageable pageable);

    /**
     * Normalizes a city name for searching
     * Converts to lowercase and removes accents
     * 
     * @param city City name to normalize
     * @return Normalized city name
     */
    String normalizeCity(String city);
}