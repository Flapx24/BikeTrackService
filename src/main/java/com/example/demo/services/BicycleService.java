package com.example.demo.services;

import java.util.Date;
import java.util.List;

import com.example.demo.entities.Bicycle;
import com.example.demo.entities.BicycleComponent;

public interface BicycleService {
    
    Bicycle saveBicycle(Bicycle bicycle);
    
    Bicycle findById(Long id);
    
    void deleteBicycle(Long id);
    
    Bicycle initializeWithDefaultComponents(Bicycle bicycle);
    
    Bicycle initializeWithCustomComponents(Bicycle bicycle, List<BicycleComponent> components);
    
    void addKilometers(Long bicycleId, Double kilometers);
    
    BicycleComponent addComponent(Long bicycleId, BicycleComponent component);
    
    boolean removeComponent(Long bicycleId, Long componentId);
    
    void removeAllComponents(Long bicycleId);
    
    List<BicycleComponent> getComponentsNeedingMaintenance(Long bicycleId);

    boolean updateMaintenanceDate(Long bicycleId);
    
    boolean updateMaintenanceDate(Long bicycleId, Date maintenanceDate);
}
