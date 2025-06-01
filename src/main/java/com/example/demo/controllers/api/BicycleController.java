package com.example.demo.controllers.api;

import java.util.List;
import java.util.Map;
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
import com.example.demo.dtos.BicycleSummaryDTO;
import com.example.demo.entities.Bicycle;
import com.example.demo.entities.User;
import com.example.demo.services.BicycleService;
import com.example.demo.services.UserService;
import com.example.demo.upload.StorageService;
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

        @Autowired
        @Qualifier("storageService")
        private StorageService storageService;

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

                String randomBicycleImageUrl = storageService.getRandomBicycleImage();
                bicycle.setIconUrl(randomBicycleImageUrl);

                bicycle = bicycleService.saveBicycle(bicycle);

                return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                                "success", true,
                                "message", "Bicicleta creada con éxito",
                                "data", new BicycleDTO(bicycle)));
        }

        /**
         * Update an existing bicycle
         * 
         * @param authHeader Authorization token
         * @param bicycleId  ID of the bicycle to update
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
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                                        "success", false,
                                        "message", "Bicicleta no encontrada con ID: " + bicycleId));
                }

                if (!existingBicycle.getOwner().getId().equals(user.getId())) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(Map.of(
                                                        "success", false,
                                                        "message", "No tienes permiso para modificar esta bicicleta"));
                }

                List<Long> invalidComponentIds = bicycleService.validateComponentsFromDTO(bicycleDTO);
                if (!invalidComponentIds.isEmpty()) {
                        return ResponseEntity.badRequest().body(Map.of(
                                        "success", false,
                                        "message", "Los siguientes componentes no existen: " + invalidComponentIds));
                }

                bicycleDTO.setId(bicycleId);
                bicycleDTO.setOwnerId(user.getId());

                Bicycle updatedBicycle = bicycleDTO.toEntity(user);
                updatedBicycle = bicycleService.saveBicycle(updatedBicycle);

                return ResponseEntity.ok(Map.of(
                                "success", true,
                                "message", "Bicicleta actualizada con éxito",
                                "data", new BicycleDTO(updatedBicycle)));
        }

        /**
         * Delete a bicycle
         * 
         * @param authHeader Authorization token
         * @param bicycleId  ID of the bicycle to delete
         * @return 204 if successfully deleted, 404 if it doesn't exist
         */
        @DeleteMapping("/{bicycleId}")
        public ResponseEntity<?> deleteBicycle(
                        @RequestHeader("Authorization") String authHeader,
                        @PathVariable Long bicycleId) {

                User user = jwtService.getUser(authHeader);
                Bicycle bicycle = bicycleService.findById(bicycleId);

                if (bicycle == null) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(Map.of(
                                                        "success", false,
                                                        "message", "Bicicleta no encontrada con ID: " + bicycleId));
                }

                if (!bicycle.getOwner().getId().equals(user.getId())) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(Map.of(
                                                        "success", false,
                                                        "message", "No tienes permiso para eliminar esta bicicleta"));
                }

                bicycleService.deleteBicycle(bicycleId);
                return ResponseEntity.noContent().build();
        }

        /**
         * Get a bicycle by its ID
         * 
         * @param authHeader Authorization token
         * @param bicycleId  ID of the bicycle to retrieve
         * @return The requested bicycle or 404 if it doesn't exist
         */
        @GetMapping("/{bicycleId}")
        public ResponseEntity<?> getBicycle(
                        @RequestHeader("Authorization") String authHeader,
                        @PathVariable Long bicycleId) {

                User user = jwtService.getUser(authHeader);
                Bicycle bicycle = bicycleService.findById(bicycleId);

                if (bicycle == null) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                        .body(Map.of(
                                                        "success", false,
                                                        "message", "Bicicleta no encontrada con ID: " + bicycleId));
                }

                if (!bicycle.getOwner().getId().equals(user.getId())) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(Map.of(
                                                        "success", false,
                                                        "message", "No tienes permiso para ver esta bicicleta"));
                }

                return ResponseEntity.ok(Map.of(
                                "success", true,
                                "message", "Bicicleta recuperada con éxito",
                                "data", new BicycleDTO(bicycle)));
        }

        /**
         * Get all bicycles of the authenticated user
         * 
         * @param authHeader Authorization token
         * @return List of user's bicycles (summary view without components)
         */
        @GetMapping
        public ResponseEntity<?> getAllBicycles(
                        @RequestHeader("Authorization") String authHeader) {

                User user = jwtService.getUser(authHeader);
                List<Bicycle> bicycles = bicycleService.findByOwnerId(user.getId());

                List<BicycleSummaryDTO> bicycleSummaryDTOs = bicycles.stream()
                                .map(BicycleSummaryDTO::new)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(Map.of(
                                "success", true,
                                "message", "Bicicletas recuperadas con éxito",
                                "data", bicycleSummaryDTOs));
        }

        /**
         * Add kilometers to a bicycle and its components
         * 
         * @param authHeader Authorization token
         * @param bicycleId  ID of the bicycle
         * @param kilometers Kilometers to add
         * @return Updated bicycle
         */
        @PostMapping("/{bicycleId}/add-kilometers")
        public ResponseEntity<?> addKilometers(
                        @RequestHeader("Authorization") String authHeader,
                        @PathVariable Long bicycleId,
                        @RequestParam Double kilometers) {

                if (kilometers == null || kilometers <= 0) {
                        return ResponseEntity.badRequest().body(Map.of(
                                        "success", false,
                                        "message", "Los kilómetros deben ser un valor positivo"));
                }

                User user = jwtService.getUser(authHeader);
                Bicycle bicycle = bicycleService.findById(bicycleId);

                if (bicycle == null) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                                        "success", false,
                                        "message", "Bicicleta no encontrada con ID: " + bicycleId));
                }

                if (!bicycle.getOwner().getId().equals(user.getId())) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(Map.of(
                                                        "success", false,
                                                        "message", "No tienes permiso para modificar esta bicicleta"));
                }

                bicycle = bicycleService.addKilometers(bicycleId, kilometers);

                return ResponseEntity.ok(Map.of(
                                "success", true,
                                "message", "Kilómetros añadidos con éxito",
                                "data", new BicycleDTO(bicycle)));
        }

        /**
         * Subtract kilometers from a bicycle and its components
         * 
         * @param authHeader Authorization token
         * @param bicycleId  ID of the bicycle
         * @param kilometers Kilometers to subtract
         * @return Updated bicycle
         */
        @PostMapping("/{bicycleId}/subtract-kilometers")
        public ResponseEntity<?> subtractKilometers(
                        @RequestHeader("Authorization") String authHeader,
                        @PathVariable Long bicycleId,
                        @RequestParam Double kilometers) {

                if (kilometers == null || kilometers <= 0) {
                        return ResponseEntity.badRequest().body(Map.of(
                                        "success", false,
                                        "message", "Los kilómetros deben ser un valor positivo"));
                }

                User user = jwtService.getUser(authHeader);
                Bicycle bicycle = bicycleService.findById(bicycleId);

                if (bicycle == null) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                                        "success", false,
                                        "message", "Bicicleta no encontrada con ID: " + bicycleId));
                }

                if (!bicycle.getOwner().getId().equals(user.getId())) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                        .body(Map.of(
                                                        "success", false,
                                                        "message", "No tienes permiso para modificar esta bicicleta"));
                }

                bicycle = bicycleService.subtractKilometers(bicycleId, kilometers);

                return ResponseEntity.ok(Map.of(
                                "success", true,
                                "message", "Kilómetros restados con éxito",
                                "data", new BicycleDTO(bicycle)));
        }
}