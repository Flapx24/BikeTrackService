package com.example.demo.services;

import java.time.LocalDate;
import java.util.List;

import com.example.demo.dtos.BicycleDTO;
import com.example.demo.entities.Bicycle;
import com.example.demo.entities.BicycleComponent;

public interface BicycleService {
    
    Bicycle saveBicycle(Bicycle bicycle);
    
    Bicycle findById(Long id);

    List<Bicycle> findByOwnerId(Long ownerId);
    
    void deleteBicycle(Long id);
    
    Bicycle initializeWithDefaultComponents(Bicycle bicycle);
    
    Bicycle initializeWithCustomComponents(Bicycle bicycle, List<BicycleComponent> components);
    
    Bicycle addKilometers(Long bicycleId, Double kilometers);

    Bicycle subtractKilometers(Long bicycleId, Double kilometers);
    
    BicycleComponent addComponent(Long bicycleId, BicycleComponent component);
    
    boolean removeComponent(Long bicycleId, Long componentId);
    
    void removeAllComponents(Long bicycleId);
    
    List<BicycleComponent> getComponentsNeedingMaintenance(Long bicycleId);

    boolean updateMaintenanceDate(Long bicycleId);
    
    boolean updateMaintenanceDate(Long bicycleId, LocalDate maintenanceDate);
    
    /**
     * Validates all components in a list to ensure they exist in the database
     * 
     * @param componentIds List of component IDs to validate
     * @return List of invalid component IDs, empty if all are valid
     */
    List<Long> validateComponents(List<Long> componentIds);
    
    /**
     * Validates all components from a BicycleDTO
     * 
     * @param bicycleDTO The DTO containing components to validate
     * @return List of invalid component IDs, empty if all are valid
     */
    List<Long> validateComponentsFromDTO(BicycleDTO bicycleDTO);
    
}
