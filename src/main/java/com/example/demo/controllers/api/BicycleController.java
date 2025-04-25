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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dtos.BicycleDTO;
import com.example.demo.entities.Bicycle;
import com.example.demo.entities.User;
import com.example.demo.services.BicycleService;
import com.example.demo.services.UserService;
import com.example.demo.servicesImpl.JwtService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/bicycles")
public class BicycleController {

    @Autowired
    @Qualifier("bicycleService")
    private BicycleService bicycleService;
    
    @Autowired
    @Qualifier("userService")
    private UserService userService;
    
    @Autowired
    @Qualifier("jwtService")
    private JwtService jwtService;
    
    /**
     * Create a new bicycle for the authenticated user
     * 
     * @param authHeader Authorization token
     * @param bicycleDTO Bicycle data to create
     * @return Created bicycle with 201 status code
     */
    @PostMapping
    public ResponseEntity<?> createBicycle(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody BicycleDTO bicycleDTO) {
        
        User user = jwtService.getUser(authHeader);
        
        bicycleDTO.setId(null);
        bicycleDTO.setOwnerId(user.getId());
        
        Bicycle bicycle = bicycleDTO.toEntity(user);
        bicycle = bicycleService.saveBicycle(bicycle);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(new BicycleDTO(bicycle));
    }
    
    /**
     * Update an existing bicycle
     * 
     * @param authHeader Authorization token
     * @param bicycleId ID of the bicycle to update
     * @param bicycleDTO Updated bicycle data
     * @return Updated bicycle or 404 if it doesn't exist
     */
    @PutMapping("/{bicycleId}")
    public ResponseEntity<?> updateBicycle(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long bicycleId,
            @Valid @RequestBody BicycleDTO bicycleDTO) {
        
        User user = jwtService.getUser(authHeader);
        
        Bicycle existingBicycle = bicycleService.findById(bicycleId);
        if (existingBicycle == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (!existingBicycle.getOwner().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        List<Long> invalidComponentIds = bicycleService.validateComponentsFromDTO(bicycleDTO);
        if (!invalidComponentIds.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    "Los siguientes componentes no existen: " + invalidComponentIds);
        }
        
        bicycleDTO.setId(bicycleId);
        bicycleDTO.setOwnerId(user.getId());
        
        Bicycle updatedBicycle = bicycleDTO.toEntity(user);
        updatedBicycle = bicycleService.saveBicycle(updatedBicycle);
        
        return ResponseEntity.ok(new BicycleDTO(updatedBicycle));
    }
    
    /**
     * Delete a bicycle
     * 
     * @param authHeader Authorization token
     * @param bicycleId ID of the bicycle to delete
     * @return 204 if successfully deleted, 404 if it doesn't exist
     */
    @DeleteMapping("/{bicycleId}")
    public ResponseEntity<?> deleteBicycle(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long bicycleId) {
        
        User user = jwtService.getUser(authHeader);
        Bicycle bicycle = bicycleService.findById(bicycleId);
        
        if (bicycle == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (!bicycle.getOwner().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        bicycleService.deleteBicycle(bicycleId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get a bicycle by its ID
     * 
     * @param authHeader Authorization token
     * @param bicycleId ID of the bicycle to retrieve
     * @return The requested bicycle or 404 if it doesn't exist
     */
    @GetMapping("/{bicycleId}")
    public ResponseEntity<?> getBicycle(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long bicycleId) {
        
        User user = jwtService.getUser(authHeader);
        Bicycle bicycle = bicycleService.findById(bicycleId);
        
        if (bicycle == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (!bicycle.getOwner().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        return ResponseEntity.ok(new BicycleDTO(bicycle));
    }
    
    /**
     * Get all bicycles of the authenticated user
     * 
     * @param authHeader Authorization token
     * @return List of user's bicycles
     */
    @GetMapping
    public ResponseEntity<?> getAllBicycles(
            @RequestHeader("Authorization") String authHeader) {
        
        User user = jwtService.getUser(authHeader);
        List<Bicycle> bicycles = bicycleService.findByOwnerId(user.getId());
        
        List<BicycleDTO> bicycleDTOs = bicycles.stream()
                .map(BicycleDTO::new)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(bicycleDTOs);
    }
    
    /**
     * Add kilometers to a bicycle and its components
     * 
     * @param authHeader Authorization token
     * @param bicycleId ID of the bicycle
     * @param kilometers Kilometers to add
     * @return Updated bicycle
     */
    @PostMapping("/{bicycleId}/add-kilometers")
    public ResponseEntity<?> addKilometers(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long bicycleId,
            @RequestParam Double kilometers) {
        
        if (kilometers == null || kilometers <= 0) {
            return ResponseEntity.badRequest().body("Los kilómetros deben ser un valor positivo");
        }
        
        User user = jwtService.getUser(authHeader);
        Bicycle bicycle = bicycleService.findById(bicycleId);
        
        if (bicycle == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (!bicycle.getOwner().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        bicycle = bicycleService.addKilometers(bicycleId, kilometers);
        
        return ResponseEntity.ok(new BicycleDTO(bicycle));
    }
    
    /**
     * Subtract kilometers from a bicycle and its components
     * 
     * @param authHeader Authorization token
     * @param bicycleId ID of the bicycle
     * @param kilometers Kilometers to subtract
     * @return Updated bicycle
     */
    @PostMapping("/{bicycleId}/subtract-kilometers")
    public ResponseEntity<?> subtractKilometers(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long bicycleId,
            @RequestParam Double kilometers) {
        
        if (kilometers == null || kilometers <= 0) {
            return ResponseEntity.badRequest().body("Los kilómetros deben ser un valor positivo");
        }
        
        User user = jwtService.getUser(authHeader);
        Bicycle bicycle = bicycleService.findById(bicycleId);
        
        if (bicycle == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (!bicycle.getOwner().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        bicycle = bicycleService.subtractKilometers(bicycleId, kilometers);
        
        return ResponseEntity.ok(new BicycleDTO(bicycle));
    }
}