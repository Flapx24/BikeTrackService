package com.example.demo.servicesImpl;

import java.util.Date;

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
        
        bicycle.setLastMaintenanceDate(new Date());
        bicycleService.saveBicycle(bicycle);
        
        return true;
    }

    @Override
    @Transactional
    public boolean resetComponentByName(Long bicycleId, String componentName) {
        Bicycle bicycle = bicycleService.findById(bicycleId);
        if (bicycle == null) {
            return false;
        }

        BicycleComponent componentToReset = null;
        for (BicycleComponent component : bicycle.getComponents()) {
            if (component.getName().equals(componentName)) {
                componentToReset = component;
                break;
            }
        }
        
        if (componentToReset == null) {
            return false;
        }
        
        bicycleComponentService.resetCurrentKilometers(componentToReset.getId());
        
        bicycle.setLastMaintenanceDate(new Date());
        bicycleService.saveBicycle(bicycle);
        
        return true;
    }

    @Override
    @Transactional
    public boolean performMaintenanceOnComponent(Long bicycleId, Long componentId) {
        Bicycle bicycle = bicycleService.findById(bicycleId);
        if (bicycle == null) {
            return false;
        }
        
        BicycleComponent component = bicycleComponentService.findById(componentId);
        if (component == null || !component.getBicycle().getId().equals(bicycleId)) {
            return false;
        }
        
        bicycleComponentService.performMaintenance(componentId);
        return true;
    }

    @Override
    @Transactional
    public boolean performMaintenanceOnComponentByName(Long bicycleId, String componentName) {
        Bicycle bicycle = bicycleService.findById(bicycleId);
        if (bicycle == null) {
            return false;
        }
        
        BicycleComponent componentToMaintain = null;
        for (BicycleComponent component : bicycle.getComponents()) {
            if (component.getName().equals(componentName)) {
                componentToMaintain = component;
                break;
            }
        }
        
        if (componentToMaintain == null) {
            return false;
        }
        
        bicycleComponentService.performMaintenance(componentToMaintain.getId());
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
        
        bicycle.setLastMaintenanceDate(new Date());
        bicycleService.saveBicycle(bicycle);
        
        return true;
    }

    @Override
    @Transactional
    public boolean performFullMaintenance(Long bicycleId) {
        return bicycleService.updateMaintenanceDate(bicycleId);
    }

    @Override
    public Date getLastMaintenanceDate(Long bicycleId) {
        Bicycle bicycle = bicycleService.findById(bicycleId);
        return bicycle != null ? bicycle.getLastMaintenanceDate() : null;
    }
}
