package com.example.demo.servicesImpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entities.Bicycle;
import com.example.demo.entities.BicycleComponent;
import com.example.demo.repositories.BicycleRepository;
import com.example.demo.services.BicycleComponentService;
import com.example.demo.services.BicycleService;

@Service("bicycleService")
public class BicycleServiceImpl implements BicycleService {

    @Autowired
    @Qualifier("bicycleRepository")
    private BicycleRepository bicycleRepository;
    
    @Autowired
    @Qualifier("bicycleComponentService")
    private BicycleComponentService bicycleComponentService;

    @Override
    public Bicycle saveBicycle(Bicycle bicycle) {
        if (bicycle.getLastMaintenanceDate() == null) {
            bicycle.setLastMaintenanceDate(new Date());
        }
        return bicycleRepository.save(bicycle);
    }

    @Override
    public Bicycle findById(Long id) {
        return bicycleRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void deleteBicycle(Long id) {
        bicycleRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Bicycle initializeWithDefaultComponents(Bicycle bicycle) {
        Bicycle savedBicycle = saveBicycle(bicycle);
        
        List<BicycleComponent> defaultComponents = bicycleComponentService.getDefaultComponents(savedBicycle);
        for (BicycleComponent component : defaultComponents) {
            bicycleComponentService.saveComponent(component);
        }

        return findById(savedBicycle.getId());
    }

    @Override
    @Transactional
    public Bicycle initializeWithCustomComponents(Bicycle bicycle, List<BicycleComponent> components) {
        Bicycle savedBicycle = saveBicycle(bicycle);
        
        for (BicycleComponent component : components) {
            component.setBicycle(savedBicycle);
            bicycleComponentService.saveComponent(component);
        }

        return findById(savedBicycle.getId());
    }

    @Override
    @Transactional
    public void addKilometers(Long bicycleId, Double kilometers) {
        if (kilometers <= 0) {
            return;
        }
        
        Bicycle bicycle = findById(bicycleId);
        if (bicycle == null) {
            return;
        }

        bicycle.setTotalKilometers(bicycle.getTotalKilometers() + kilometers);
        saveBicycle(bicycle);

        for (BicycleComponent component : bicycle.getComponents()) {
            bicycleComponentService.addKilometers(component.getId(), kilometers);
        }
    }

    @Override
    @Transactional
    public BicycleComponent addComponent(Long bicycleId, BicycleComponent component) {
        Bicycle bicycle = findById(bicycleId);
        if (bicycle == null) {
            return null;
        }
        
        component.setBicycle(bicycle);
        return bicycleComponentService.saveComponent(component);
    }

    @Override
    @Transactional
    public boolean removeComponent(Long bicycleId, Long componentId) {
        Bicycle bicycle = findById(bicycleId);
        if (bicycle == null) {
            return false;
        }
        
        BicycleComponent component = bicycleComponentService.findById(componentId);
        if (component == null || !component.getBicycle().getId().equals(bicycleId)) {
            return false;
        }
        
        bicycleComponentService.deleteComponent(componentId);
        return true;
    }

    @Override
    @Transactional
    public boolean removeComponentByName(Long bicycleId, String componentName) {
        Bicycle bicycle = findById(bicycleId);
        if (bicycle == null) {
            return false;
        }

        BicycleComponent componentToRemove = null;
        for (BicycleComponent component : bicycle.getComponents()) {
            if (component.getName().equals(componentName)) {
                componentToRemove = component;
                break;
            }
        }
        
        if (componentToRemove == null) {
            return false;
        }
        
        bicycleComponentService.deleteComponent(componentToRemove.getId());
        return true;
    }

    @Override
    @Transactional
    public void removeAllComponents(Long bicycleId) {
        Bicycle bicycle = findById(bicycleId);
        if (bicycle == null) {
            return;
        }

        List<BicycleComponent> components = new ArrayList<>(bicycle.getComponents());
        for (BicycleComponent component : components) {
            bicycleComponentService.deleteComponent(component.getId());
        }
    }

    @Override
    public List<BicycleComponent> getComponentsNeedingMaintenance(Long bicycleId) {
        Bicycle bicycle = findById(bicycleId);
        if (bicycle == null) {
            return new ArrayList<>();
        }

        List<BicycleComponent> needMaintenance = new ArrayList<>();
        for (BicycleComponent component : bicycle.getComponents()) {
            if (bicycleComponentService.needsMaintenance(component.getId())) {
                needMaintenance.add(component);
            }
        }
        
        return needMaintenance;
    }

    @Override
    @Transactional
    public boolean updateMaintenanceDate(Long bicycleId) {
        return updateMaintenanceDate(bicycleId, new Date());
    }

    @Override
    @Transactional
    public boolean updateMaintenanceDate(Long bicycleId, Date maintenanceDate) {
        Bicycle bicycle = findById(bicycleId);
        if (bicycle == null) {
            return false;
        }
        
        bicycle.setLastMaintenanceDate(maintenanceDate);
        saveBicycle(bicycle);
        return true;
    }
}
