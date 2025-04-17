package com.example.demo.servicesImpl;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entities.Bicycle;
import com.example.demo.entities.BicycleComponent;
import com.example.demo.services.BicycleComponentService;
import com.example.demo.services.BicycleMaintenanceService;
import com.example.demo.services.BicycleService;

@Service("bicycleMaintenanceService")
public class BicycleMaintenanceServiceImpl implements BicycleMaintenanceService {

    @Autowired
    @Qualifier("bicycleService")
    private BicycleService bicycleService;
    
    @Autowired
    @Qualifier("bicycleComponentService")
    private BicycleComponentService bicycleComponentService;

    @Override
    @Transactional
    public boolean resetComponent(Long bicycleId, Long componentId) {
        Bicycle bicycle = bicycleService.findById(bicycleId);
        if (bicycle == null) {
            return false;
        }
        
        BicycleComponent component = bicycleComponentService.findById(componentId);
        if (component == null || !component.getBicycle().getId().equals(bicycleId)) {
            return false;
        }

        bicycleComponentService.resetCurrentKilometers(componentId);
        return true;
    }

    @Override
    @Transactional
    public boolean updateComponentMaxKilometers(Long bicycleId, Long componentId, Double maxKilometers) {
        if (maxKilometers == null || maxKilometers <= 0) {
            return false;
        }
        
        Bicycle bicycle = bicycleService.findById(bicycleId);
        if (bicycle == null) {
            return false;
        }
        
        BicycleComponent component = bicycleComponentService.findById(componentId);
        if (component == null || !component.getBicycle().getId().equals(bicycleId)) {
            return false;
        }

        component.setMaxKilometers(maxKilometers);
        bicycleComponentService.saveComponent(component);

        bicycle.setLastMaintenanceDate(LocalDate.now());
        bicycleService.saveBicycle(bicycle);
        
        return true;
    }

    @Override
    @Transactional
    public boolean registerMaintenance(Long bicycleId) {
        return bicycleService.updateMaintenanceDate(bicycleId);
    }

    @Override
    @Transactional
    public boolean registerMaintenanceWithDate(Long bicycleId, LocalDate maintenanceDate) {
        if (maintenanceDate == null) {
            return false;
        }
        return bicycleService.updateMaintenanceDate(bicycleId, maintenanceDate);
    }

    @Override
    public LocalDate getLastMaintenanceDate(Long bicycleId) {
        Bicycle bicycle = bicycleService.findById(bicycleId);
        return bicycle != null ? bicycle.getLastMaintenanceDate() : null;
    }
}
