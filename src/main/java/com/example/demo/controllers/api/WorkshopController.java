package com.example.demo.controllers.api;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.example.demo.dtos.WorkshopDTO;
import com.example.demo.entities.Workshop;
import com.example.demo.services.WorkshopService;

@RestController
@RequestMapping("/api/workshops")
public class WorkshopController {

    private static final Logger logger = LoggerFactory.getLogger(WorkshopController.class);

    @Autowired
    @Qualifier("workshopService")
    private WorkshopService workshopService;

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "success", false,
                "message", "Error en formato de datos del taller: " + ex.getMessage()));
    }

    /**
     * Get a workshop by its ID
     * 
     * @param workshopId ID of the workshop to retrieve
     * @return The requested workshop or 404 if it doesn't exist
     */
    @GetMapping("/{workshopId}")
    public ResponseEntity<?> getWorkshop(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long workshopId) {

        Workshop workshop = workshopService.findById(workshopId);

        if (workshop == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "Taller no encontrado con ID: " + workshopId));
        }

        try {
            WorkshopDTO workshopDTO = new WorkshopDTO(workshop);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Taller recuperado con éxito",
                    "data", workshopDTO));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", "Error en los datos del taller: " + e.getMessage()));
        }
    }

    /**
     * Get all workshops for a specific city
     * 
     * @param city Name of the city (case insensitive)
     * @return List of workshops in the city
     */
    @GetMapping("/city")
    public ResponseEntity<?> getWorkshopsByCity(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String city) {

        if (city == null || city.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "El nombre de la ciudad es obligatorio"));
        }

        List<Workshop> workshops = workshopService.findByCity(city);

        if (workshops.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false,
                    "message", "No se encontraron talleres en la ciudad: " + city));
        }        List<WorkshopDTO> workshopDTOs = new ArrayList<>();
        for (Workshop workshop : workshops) {
            try {
                workshopDTOs.add(new WorkshopDTO(workshop));
            } catch (IllegalArgumentException e) {
                logger.warn("Error processing workshop ID {}: {}", workshop.getId(), e.getMessage());
            }
        }

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Talleres recuperados con éxito",
                "data", workshopDTOs));
    }
}