package com.example.demo.controllers.api;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dtos.RouteUpdateDTO;
import com.example.demo.entities.Route;
import com.example.demo.entities.RouteUpdate;
import com.example.demo.entities.User;
import com.example.demo.services.RouteService;
import com.example.demo.services.RouteUpdateService;
import com.example.demo.servicesImpl.JwtService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/route-updates")
public class RouteUpdateController {

    @Autowired
    @Qualifier("routeUpdateService")
    private RouteUpdateService routeUpdateService;
    
    @Autowired
    @Qualifier("routeService")
    private RouteService routeService;
    
    @Autowired
    @Qualifier("jwtService")
    private JwtService jwtService;
    
    /**
     * Create a new route update
     * 
     * @param authHeader Authorization token
     * @param routeUpdateDTO Route update data to create
     * @return Created route update with 201 status code
     */
    @PostMapping
    public ResponseEntity<?> createRouteUpdate(
            @RequestHeader("Authorization") String authHeader,
@Valid @RequestBody RouteUpdateDTO routeUpdateDTO) {

        if (routeUpdateDTO.getRouteId() == null) {
            return ResponseEntity.badRequest().body("El ID de la ruta es obligatorio para crear una actualización de ruta");
        }

        Route route = routeService.findById(routeUpdateDTO.getRouteId());
        if (route == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ruta no encontrada con ID: " + routeUpdateDTO.getRouteId());
        }

        User currentUser = jwtService.getUser(authHeader);

        routeUpdateDTO.setId(null);

        RouteUpdate routeUpdate = routeUpdateDTO.toEntity();
        routeUpdate.setRoute(route);
        routeUpdate.setUser(currentUser);
        
        routeUpdate = routeUpdateService.saveRouteUpdate(routeUpdate);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(new RouteUpdateDTO(routeUpdate));
    }
    
    /**
     * Update an existing route update
     * 
     * @param authHeader Authorization token
     * @param routeUpdateDTO Updated route update data
     * @return Updated route update or appropriate error status
     */
    @PutMapping
    public ResponseEntity<?> updateRouteUpdate(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody RouteUpdateDTO routeUpdateDTO) {

        if (routeUpdateDTO.getId() == null) {
            return ResponseEntity.badRequest().body("El ID de la actualización de ruta es obligatorio para la actualización");
        }

        RouteUpdate existingRouteUpdate = routeUpdateService.findById(routeUpdateDTO.getId());
        if (existingRouteUpdate == null) {
            return ResponseEntity.notFound().build();
        }

        Route existingRoute = existingRouteUpdate.getRoute();
        if (existingRoute == null) {
            return ResponseEntity.badRequest().body("La actualización de ruta existente no tiene una ruta asociada");
        }
        
        User currentUser = jwtService.getUser(authHeader);
        
        User owner = existingRouteUpdate.getUser();
        if (owner == null || !owner.getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("No tienes permiso para modificar esta actualización de ruta");
        }
        
        RouteUpdate routeUpdate = routeUpdateDTO.toEntity();
        routeUpdate.setId(existingRouteUpdate.getId());
        routeUpdate.setRoute(existingRoute);
        routeUpdate.setUser(currentUser);
        
        routeUpdate = routeUpdateService.saveRouteUpdate(routeUpdate);
        
        return ResponseEntity.ok(new RouteUpdateDTO(routeUpdate));
    }
    
    /**
     * Delete a route update
     * 
     * @param authHeader Authorization token
     * @param routeUpdateId ID of the route update to delete
     * @return Empty response with appropriate status
     */
    @DeleteMapping("/{routeUpdateId}")
    public ResponseEntity<?> deleteRouteUpdate(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long routeUpdateId) {
        
        RouteUpdate routeUpdate = routeUpdateService.findById(routeUpdateId);
        if (routeUpdate == null) {
            return ResponseEntity.notFound().build();
        }
        
        User currentUser = jwtService.getUser(authHeader);
        
        User owner = routeUpdate.getUser();
        if (owner == null || !owner.getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("No tienes permiso para eliminar esta actualización de ruta");
        }
        
        boolean deleted = routeUpdateService.deleteRouteUpdate(routeUpdateId);
        
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
    
    /**
     * Get a route update by its ID
     * 
     * @param authHeader Authorization token
     * @param routeUpdateId ID of the route update to retrieve
     * @return The requested route update or 404 if it doesn't exist
     */
    @GetMapping("/{routeUpdateId}")
    public ResponseEntity<?> getRouteUpdate(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long routeUpdateId) {
        
        RouteUpdate routeUpdate = routeUpdateService.findById(routeUpdateId);
        if (routeUpdate == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(new RouteUpdateDTO(routeUpdate));
    }
    
    /**
     * Get all route updates for a route
     * 
     * @param authHeader Authorization token
     * @param routeId ID of the route
     * @return List of route updates
     */
    @GetMapping("/route/{routeId}")
    public ResponseEntity<?> getRouteUpdatesByRoute(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long routeId) {
        
        Route route = routeService.findById(routeId);
        if (route == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<RouteUpdate> routeUpdates = routeUpdateService.findByRouteId(routeId);
        
        List<RouteUpdateDTO> routeUpdateDTOs = routeUpdates.stream()
                .map(RouteUpdateDTO::new)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(routeUpdateDTOs);
    }
}