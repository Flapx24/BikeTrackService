package com.example.demo.controllers.api;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dtos.RouteDTO;
import com.example.demo.entities.Route;
import com.example.demo.enums.RouteDetailLevel;
import com.example.demo.services.RouteService;
import com.example.demo.servicesImpl.JwtService;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private static final int INITIAL_REVIEWS_LIMIT = 15;

    @Autowired
    @Qualifier("routeService")
    private RouteService routeService;

    @Autowired
    @Qualifier("jwtService")
    private JwtService jwtService;

    /**
     * Get a route by its ID with all details (reviews, updates, etc.)
     * Limited to first 15 reviews, additional reviews should be fetched with the reviews endpoint
     * 
     * @param authHeader Authorization token
     * @param routeId ID of the route to retrieve
     * @return The route with all details or 404 if it doesn't exist
     */
    @GetMapping("/{routeId}")
    public ResponseEntity<?> getRoute(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long routeId) {
        
        Route route = routeService.findById(routeId);
        if (route == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                    "success", false,
                    "message", "Ruta no encontrada con ID: " + routeId
                ));
        }

        RouteDTO routeDTO = RouteDTO.fromEntity(route, RouteDetailLevel.FULL);

        routeDTO.setReviewCount(route.getReviews() != null ? route.getReviews().size() : 0);
        routeDTO.setUpdateCount(route.getUpdates() != null ? route.getUpdates().size() : 0);
        
        if (routeDTO.getReviews() != null && routeDTO.getReviews().size() > INITIAL_REVIEWS_LIMIT) {
            routeDTO.setReviews(routeDTO.getReviews().subList(0, INITIAL_REVIEWS_LIMIT));
        }
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Ruta recuperada con éxito",
            "data", routeDTO
        ));
    }

    /**
     * Get all routes, with pagination option
     * 
     * @param authHeader Authorization token
     * @param lastRouteId ID of the last route received (optional, for pagination)
     * @return List of routes
     */
    @GetMapping
    public ResponseEntity<?> getAllRoutes(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) Long lastRouteId) {

        List<Route> routes = routeService.getAllRoutes(lastRouteId);
        List<RouteDTO> routeDTOs = routes.stream()
                .map(route -> {
                    RouteDTO dto = RouteDTO.fromEntity(route, RouteDetailLevel.BASIC);
                    dto.setReviewCount(route.getReviews() != null ? route.getReviews().size() : 0);
                    dto.setUpdateCount(route.getUpdates() != null ? route.getUpdates().size() : 0);
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Rutas recuperadas con éxito",
            "data", routeDTOs
        ));
    }

    /**
     * Get routes filtered by city and minimum rating
     * 
     * @param authHeader Authorization token
     * @param city City to filter
     * @param minScore Minimum score (1-5)
     * @param lastRouteId ID of the last route received (optional, for pagination)
     * @return List of routes matching the criteria
     */
    @GetMapping("/filter")
    public ResponseEntity<?> getRoutesByCityAndScore(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String city,
            @RequestParam(defaultValue = "1") Integer minScore,
            @RequestParam(required = false) Long lastRouteId) {

        if (minScore < 1 || minScore > 5) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "La puntuación debe estar entre 1 y 5"
            ));
        }
        if (city == null || city.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "El nombre de la ciudad es obligatorio"
            ));
        }
        
        List<Route> routes = routeService.getRoutesByCityAndMinScore(city, minScore, lastRouteId);
        
        List<RouteDTO> routeDTOs = routes.stream()
                .map(route -> {
                    RouteDTO dto = RouteDTO.fromEntity(route, RouteDetailLevel.BASIC);
                    dto.setReviewCount(route.getReviews() != null ? route.getReviews().size() : 0);
                    dto.setUpdateCount(route.getUpdates() != null ? route.getUpdates().size() : 0);
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Rutas filtradas recuperadas con éxito",
            "data", routeDTOs
        ));
    }

}