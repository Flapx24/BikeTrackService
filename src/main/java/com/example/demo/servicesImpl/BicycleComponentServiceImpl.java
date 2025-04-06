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
import com.example.demo.repositories.BicycleComponentRepository;
import com.example.demo.services.BicycleComponentService;
import com.example.demo.services.BicycleService;

@Service("bicycleComponentService")
public class BicycleComponentServiceImpl implements BicycleComponentService {

    @Autowired
    @Qualifier("bicycleComponentRepository")
    private BicycleComponentRepository bicycleComponentRepository;
    
    @Autowired
    @Qualifier("bicycleService")
    private BicycleService bicycleService;

    @Override
    public BicycleComponent saveComponent(BicycleComponent component) {
        return bicycleComponentRepository.save(component);
    }

    @Override
    public BicycleComponent findById(Long id) {
        return bicycleComponentRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void deleteComponent(Long id) {
        bicycleComponentRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void addKilometers(Long componentId, Double kilometers) {
        if (kilometers <= 0) {
            return;
        }
        
        BicycleComponent component = findById(componentId);
        if (component != null) {
            component.setCurrentKilometers(component.getCurrentKilometers() + kilometers);
            saveComponent(component);
        }
    }

    @Override
    @Transactional
    public void resetCurrentKilometers(Long componentId) {
        BicycleComponent component = findById(componentId);
        if (component != null) {
            component.setCurrentKilometers(0.0);
            saveComponent(component);

        }
    }

    @Override
    @Transactional
    public void performMaintenance(Long componentId) {
        BicycleComponent component = findById(componentId);
        if (component == null) {
            return;
        }

        component.setCurrentKilometers(0.0);
        saveComponent(component);

    }

    @Override
    @Transactional
    public boolean updateMaxKilometers(Long componentId, Double maxKilometers) {
        if (maxKilometers == null || maxKilometers <= 0) {
            return false;
        }
        
        BicycleComponent component = findById(componentId);
        if (component == null) {
            return false;
        }
        
        component.setMaxKilometers(maxKilometers);
        saveComponent(component);
        
        return true;
    }

    @Override
    public Double getWearPercentage(Long componentId) {
        BicycleComponent component = findById(componentId);
        if (component == null || component.getMaxKilometers() <= 0) {
            return 0.0;
        }
        
        double percentage = (component.getCurrentKilometers() / component.getMaxKilometers()) * 100;
        return Math.min(100.0, Math.max(0.0, percentage));
    }

    @Override
    public Double getRemainingKilometers(Long componentId) {
        BicycleComponent component = findById(componentId);
        if (component == null) {
            return 0.0;
        }
        
        return Math.max(0.0, component.getMaxKilometers() - component.getCurrentKilometers());
    }

    @Override
    public boolean needsMaintenance(Long componentId) {
        BicycleComponent component = findById(componentId);
        if (component == null) {
            return false;
        }
        
        return component.getCurrentKilometers() >= component.getMaxKilometers();
    }

    @Override
    public List<BicycleComponent> getDefaultComponents(Bicycle bicycle) {
        List<BicycleComponent> defaultComponents = new ArrayList<>();
        
        // Drive train components
        defaultComponents.add(new BicycleComponent(null, "Chain", 2500.0, 0.0, bicycle));
        defaultComponents.add(new BicycleComponent(null, "Cassette", 5000.0, 0.0, bicycle));
        defaultComponents.add(new BicycleComponent(null, "Chainrings", 10000.0, 0.0, bicycle));
        
        // Brake components
        defaultComponents.add(new BicycleComponent(null, "Brake Pads Front", 1500.0, 0.0, bicycle));
        defaultComponents.add(new BicycleComponent(null, "Brake Pads Rear", 1500.0, 0.0, bicycle));
        defaultComponents.add(new BicycleComponent(null, "Brake Fluid", 5000.0, 0.0, bicycle));
        
        // Suspension components
        defaultComponents.add(new BicycleComponent(null, "Fork Service", 3000.0, 0.0, bicycle));
        defaultComponents.add(new BicycleComponent(null, "Shock Service", 3000.0, 0.0, bicycle));
        
        // Wheels and tires
        defaultComponents.add(new BicycleComponent(null, "Front Tire", 2000.0, 0.0, bicycle));
        defaultComponents.add(new BicycleComponent(null, "Rear Tire", 1500.0, 0.0, bicycle));
        defaultComponents.add(new BicycleComponent(null, "Wheel Bearings", 8000.0, 0.0, bicycle));
        
        // Other components
        defaultComponents.add(new BicycleComponent(null, "Bottom Bracket", 5000.0, 0.0, bicycle));
        defaultComponents.add(new BicycleComponent(null, "Headset", 10000.0, 0.0, bicycle));
        defaultComponents.add(new BicycleComponent(null, "Dropper Post Service", 3000.0, 0.0, bicycle));
        
        return defaultComponents;
    }
}
