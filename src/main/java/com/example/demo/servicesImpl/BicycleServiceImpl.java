package com.example.demo.servicesImpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

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
    @Transactional
    public Bicycle saveBicycle(Bicycle bicycle) {
        return bicycleRepository.save(bicycle);
    }

    @Override
    public Bicycle findById(Long id) {
        return bicycleRepository.findById(id).orElse(null);
    }

    @Override
    public List<Bicycle> findByOwnerId(Long ownerId) {
        if (ownerId == null) {
            throw new IllegalArgumentException("Owner ID cannot be null");
        }
        
        return bicycleRepository.findAll().stream()
                .filter(bicycle -> bicycle.getOwner() != null && bicycle.getOwner().getId().equals(ownerId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteBicycle(Long id) {
        bicycleRepository.deleteById(id);
    }

    @Override
    public Bicycle initializeWithDefaultComponents(Bicycle bicycle) {
        List<BicycleComponent> defaultComponents = bicycleComponentService.getDefaultComponents(bicycle);
        bicycle.setComponents(defaultComponents);
        return bicycle;
    }

    @Override
    public Bicycle initializeWithCustomComponents(Bicycle bicycle, List<BicycleComponent> components) {
        for (BicycleComponent component : components) {
            component.setBicycle(bicycle);
        }
        bicycle.setComponents(components);
        return bicycle;
    }

    @Override
    @Transactional
    public Bicycle addKilometers(Long bicycleId, Double kilometers) {
        if (kilometers <= 0) {
            return null;
        }
        
        Bicycle bicycle = findById(bicycleId);
        if (bicycle == null) {
            return null;
        }

        bicycle.setTotalKilometers(bicycle.getTotalKilometers() + kilometers);

        for (BicycleComponent component : bicycle.getComponents()) {
            bicycleComponentService.addKilometers(component.getId(), kilometers);
        }

        return saveBicycle(bicycle);
    }

    @Override
    @Transactional
    public Bicycle subtractKilometers(Long bicycleId, Double kilometers) {
        if (kilometers <= 0) {
            return null;
        }
        
        Bicycle bicycle = findById(bicycleId);
        if (bicycle == null) {
            return null;
        }

        Double newTotalKm = Math.max(0.0, bicycle.getTotalKilometers() - kilometers);
        bicycle.setTotalKilometers(newTotalKm);

        for (BicycleComponent component : bicycle.getComponents()) {
            Double newComponentKm = Math.max(0.0, component.getCurrentKilometers() - kilometers);
            component.setCurrentKilometers(newComponentKm);
            bicycleComponentService.saveComponent(component);
        }

        return saveBicycle(bicycle);
    }

    @Override
    @Transactional
    public BicycleComponent addComponent(Long bicycleId, BicycleComponent component) {
        Bicycle bicycle = findById(bicycleId);
        if (bicycle == null) {
            return null;
        }
        
        component.setBicycle(bicycle);
        bicycle.getComponents().add(component);

        saveBicycle(bicycle);
        
        return component;
    }

    @Override
    @Transactional
    public boolean removeComponent(Long bicycleId, Long componentId) {
        Bicycle bicycle = findById(bicycleId);
        if (bicycle == null) {
            return false;
        }

        BicycleComponent componentToRemove = null;
        for (BicycleComponent component : bicycle.getComponents()) {
            if (component.getId().equals(componentId)) {
                componentToRemove = component;
                break;
            }
        }
        
        if (componentToRemove == null) {
            return false;
        }

        bicycle.getComponents().remove(componentToRemove);
        saveBicycle(bicycle);
        
        return true;
    }

    @Override
    @Transactional
    public void removeAllComponents(Long bicycleId) {
        Bicycle bicycle = findById(bicycleId);
        if (bicycle == null) {
            return;
        }

        bicycle.getComponents().clear();
        saveBicycle(bicycle);
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
