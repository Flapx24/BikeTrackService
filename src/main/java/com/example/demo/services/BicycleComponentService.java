package com.example.demo.services;

import java.util.List;

import com.example.demo.entities.Bicycle;
import com.example.demo.entities.BicycleComponent;

public interface BicycleComponentService {
    
    BicycleComponent saveComponent(BicycleComponent component);
    
    BicycleComponent findById(Long id);
    
    void deleteComponent(Long id);
    
    void addKilometers(Long componentId, Double kilometers);
    
    void resetCurrentKilometers(Long componentId);
    
    void performMaintenance(Long componentId);
    
    boolean updateMaxKilometers(Long componentId, Double maxKilometers);
    
    Double getWearPercentage(Long componentId);
    
    Double getRemainingKilometers(Long componentId);
    
    boolean needsMaintenance(Long componentId);
    
    List<BicycleComponent> getDefaultComponents(Bicycle bicycle);
}
