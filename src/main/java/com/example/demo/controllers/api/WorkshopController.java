package com.example.demo.controllers.api;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dtos.WorkshopDTO;
import com.example.demo.entities.Workshop;
import com.example.demo.services.WorkshopService;

@RestController
@RequestMapping("/api/workshops")
public class WorkshopController {

    @Autowired
    @Qualifier("workshopService")
    private WorkshopService workshopService;
    
    /**
     * Get a workshop by its ID
     * 
     * @param workshopId ID of the workshop to retrieve
     * @return The requested workshop or 404 if it doesn't exist
     */
    @GetMapping("/{workshopId}")
    public ResponseEntity<?> getWorkshop(
            @PathVariable Long workshopId) {
        
        Workshop workshop = workshopService.findById(workshopId);
        
        if (workshop == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(new WorkshopDTO(workshop));
    }
    
    /**
     * Get all workshops for a specific city
     * 
     * @param city Name of the city (case insensitive)
     * @return List of workshops in the city
     */
    @GetMapping("/city")
    public ResponseEntity<?> getWorkshopsByCity(
            @RequestParam String city) {
        
        List<Workshop> workshops = workshopService.findByCity(city);

        if(workshops.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        List<WorkshopDTO> workshopDTOs = workshops.stream()
                .map(WorkshopDTO::new)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(workshopDTOs);
    }
}