package com.example.demo.services;

import java.util.List;

import com.example.demo.dtos.BicycleComponentDTO;
import com.example.demo.entities.Bicycle;
import com.example.demo.entities.BicycleComponent;

public interface BicycleComponentService {
    
    BicycleComponent saveComponent(BicycleComponent component);
    
    BicycleComponent createComponentFromDTO(BicycleComponentDTO dto, Long bicycleId);
    
    BicycleComponent updateComponent(BicycleComponent component);
    
    BicycleComponent updateComponentFromDTO(BicycleComponentDTO dto, Long componentId);
    
    BicycleComponent findById(Long id);
    
    boolean deleteComponent(Long id);
    
    boolean addKilometers(Long componentId, Double kilometers);
    
    boolean resetCurrentKilometers(Long componentId);
    
    Double getWearPercentage(Long componentId);
    
    Double getRemainingKilometers(Long componentId);
    
    Boolean needsMaintenance(Long componentId);
    
    List<BicycleComponent> getDefaultComponents(Bicycle bicycle);

    boolean resetComponentsCurrentKilometers(Long bicycleId);
}
