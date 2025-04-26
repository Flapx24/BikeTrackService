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
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dtos.BicycleComponentDTO;
import com.example.demo.entities.Bicycle;
import com.example.demo.entities.BicycleComponent;
import com.example.demo.entities.User;
import com.example.demo.repositories.BicycleRepository;
import com.example.demo.services.BicycleComponentService;
import com.example.demo.servicesImpl.JwtService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/components")
public class BicycleComponentController {

    @Autowired
    @Qualifier("bicycleComponentService")
    private BicycleComponentService bicycleComponentService;
    
    @Autowired
    @Qualifier("bicycleRepository")
    private BicycleRepository bicycleRepository;
    
    @Autowired
    @Qualifier("jwtService")
    private JwtService jwtService;
    
    /**
     * Create a new component for a bicycle
     * 
     * @param authHeader Authorization header containing JWT token
     * @param bicycleId ID of the bicycle to add the component to
     * @param componentDTO Component data
     * @return Created component
     */
    @PostMapping("/bicycle/{bicycleId}")
    public ResponseEntity<?> createComponent(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long bicycleId,
            @Valid @RequestBody BicycleComponentDTO componentDTO) {
        
        User user = jwtService.getUser(authHeader);
        Bicycle bicycle = bicycleRepository.findById(bicycleId).orElse(null);
        
        if (bicycle == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false,
                "message", "Bicicleta no encontrada con ID: " + bicycleId
            ));
        }
        
        if (!bicycle.getOwner().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "success", false,
                "message", "No tienes permiso para añadir componentes a esta bicicleta"
            ));
        }
        
        BicycleComponent component = bicycleComponentService.createComponentFromDTO(componentDTO, bicycleId);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
            "success", true,
            "message", "Componente creado con éxito",
            "data", new BicycleComponentDTO(component)
        ));
    }
    
    /**
     * Update an existing component
     * 
     * @param authHeader Authorization header containing JWT token
     * @param componentId ID of the component to update
     * @param componentDTO Updated component data
     * @return Updated component
     */
    @PutMapping("/{componentId}")
    public ResponseEntity<?> updateComponent(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long componentId,
            @Valid @RequestBody BicycleComponentDTO componentDTO) {
        
        User user = jwtService.getUser(authHeader);
        BicycleComponent existingComponent = bicycleComponentService.findById(componentId);
        
        if (existingComponent == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false,
                "message", "Componente no encontrado con ID: " + componentId
            ));
        }
        
        Bicycle bicycle = existingComponent.getBicycle();
        if (!bicycle.getOwner().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "success", false,
                "message", "No tienes permiso para modificar este componente"
            ));
        }
        
        BicycleComponent updatedComponent = bicycleComponentService.updateComponentFromDTO(componentDTO, componentId);
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Componente actualizado con éxito",
            "data", new BicycleComponentDTO(updatedComponent)
        ));
    }
    
    /**
     * Delete a component
     * 
     * @param authHeader Authorization header containing JWT token
     * @param componentId ID of the component to delete
     * @return Empty response with appropriate status
     */
    @DeleteMapping("/{componentId}")
    public ResponseEntity<?> deleteComponent(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long componentId) {
        
        User user = jwtService.getUser(authHeader);
        BicycleComponent component = bicycleComponentService.findById(componentId);

        if (component == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false,
                "message", "Componente no encontrado con ID: " + componentId
            ));
        }

        if (!component.getBicycle().getOwner().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "success", false,
                "message", "No tienes permiso para eliminar este componente"
            ));
        }
        
        boolean deleted = bicycleComponentService.deleteComponent(componentId);
        
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "No se pudo eliminar el componente"
            ));
        }
        
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Get a component by ID
     * 
     * @param authHeader Authorization header containing JWT token
     * @param componentId ID of the component to retrieve
     * @return The requested component
     */
    @GetMapping("/{componentId}")
    public ResponseEntity<?> getComponent(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long componentId) {
        
        User user = jwtService.getUser(authHeader);
        BicycleComponent component = bicycleComponentService.findById(componentId);

        if (component == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false,
                "message", "Componente no encontrado con ID: " + componentId
            ));
        }

        if (!component.getBicycle().getOwner().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "success", false,
                "message", "No tienes permiso para acceder a este componente"
            ));
        }
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Componente recuperado con éxito",
            "data", new BicycleComponentDTO(component)
        ));
    }
    
    /**
     * Get all components for a bicycle
     * 
     * @param authHeader Authorization header containing JWT token
     * @param bicycleId ID of the bicycle to get components for
     * @return List of components
     */
    @GetMapping("/bicycle/{bicycleId}")
    public ResponseEntity<?> getAllComponentsForBicycle(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long bicycleId) {
        
        User user = jwtService.getUser(authHeader);
        Bicycle bicycle = bicycleRepository.findById(bicycleId).orElse(null);
        
        if (bicycle == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false,
                "message", "Bicicleta no encontrada con ID: " + bicycleId
            ));
        }
        
        if (!bicycle.getOwner().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "success", false,
                "message", "No tienes permiso para ver los componentes de esta bicicleta"
            ));
        }
        
        List<BicycleComponentDTO> componentDTOs = bicycle.getComponents().stream()
                .map(BicycleComponentDTO::new)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Componentes recuperados con éxito",
            "data", componentDTOs
        ));
    }
    
    /**
     * Reset kilometers for all components of a bicycle
     * 
     * @param authHeader Authorization header containing JWT token
     * @param bicycleId ID of the bicycle
     * @return Success status
     */
    @PostMapping("/bicycle/{bicycleId}/reset")
    public ResponseEntity<?> resetComponentsKilometers(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long bicycleId) {
        
        User user = jwtService.getUser(authHeader);
        Bicycle bicycle = bicycleRepository.findById(bicycleId).orElse(null);
        
        if (bicycle == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false,
                "message", "Bicicleta no encontrada con ID: " + bicycleId
            ));
        }
        
        if (!bicycle.getOwner().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "success", false,
                "message", "No tienes permiso para reiniciar los componentes de esta bicicleta"
            ));
        }
        
        boolean reset = bicycleComponentService.resetComponentsCurrentKilometers(bicycleId);
        
        if (!reset) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false,
                "message", "No se pudieron reiniciar los kilómetros de los componentes"
            ));
        }
        
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Kilómetros de los componentes reiniciados con éxito"
        ));
    }
}
