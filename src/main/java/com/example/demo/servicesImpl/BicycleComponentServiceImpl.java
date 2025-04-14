package com.example.demo.servicesImpl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dtos.BicycleComponentDTO;
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
    @Lazy
    @Qualifier("bicycleService")
    private BicycleService bicycleService;

    @Override
    public BicycleComponent saveComponent(BicycleComponent component) {
        if (component == null) {
            return null;
        }
        if (component.getBicycle() == null) {
            throw new IllegalArgumentException("No se puede guardar un componente sin bicicleta asociada");
        }
        
        return bicycleComponentRepository.save(component);
    }

    @Override
    @Transactional
    public BicycleComponent createComponentFromDTO(BicycleComponentDTO dto, Long bicycleId) {
        if (dto == null || bicycleId == null) {
            return null;
        }
        
        Bicycle bicycle = bicycleService.findById(bicycleId);
        if (bicycle == null) {
            throw new IllegalArgumentException("Bicicleta no encontrada con ID: " + bicycleId);
        }

        BicycleComponent component = dto.toEntity(bicycle);
        return saveComponent(component);
    }
    
    @Override
    @Transactional
    public BicycleComponent updateComponent(BicycleComponent component) {
        if (component == null || component.getId() == null) {
            return null;
        }
        
        BicycleComponent existingComponent = findById(component.getId());
        if (existingComponent == null) {
            return null;
        }
        
        component.setBicycle(existingComponent.getBicycle());
        
        return bicycleComponentRepository.save(component);
    }

    @Override
    @Transactional
    public BicycleComponent updateComponentFromDTO(BicycleComponentDTO dto, Long componentId) {
        if (dto == null || componentId == null) {
            return null;
        }
        
        BicycleComponent existingComponent = findById(componentId);
        if (existingComponent == null) {
            return null;
        }
        
        BicycleComponent updatedComponent = dto.toEntity(existingComponent.getBicycle());
        updatedComponent.setId(componentId);
        
        return bicycleComponentRepository.save(updatedComponent);
    }

    @Override
    public BicycleComponent findById(Long id) {
        if (id == null) {
            return null;
        }
        return bicycleComponentRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public boolean deleteComponent(Long id) {
        if (id == null) {
            return false;
        }
        
        if (!bicycleComponentRepository.existsById(id)) {
            return false;
        }
        
        bicycleComponentRepository.deleteById(id);
        return true;
    }

    @Override
    @Transactional
    public boolean addKilometers(Long componentId, Double kilometers) {
        if (componentId == null || kilometers == null || kilometers <= 0) {
            return false;
        }
        
        BicycleComponent component = findById(componentId);
        if (component == null) {
            return false;
        }

        component.setCurrentKilometers(component.getCurrentKilometers() + kilometers);
        saveComponent(component);
        return true;
    }

    @Override
    @Transactional
    public boolean resetCurrentKilometers(Long componentId) {
        if (componentId == null) {
            return false;
        }
        
        BicycleComponent component = findById(componentId);
        if (component == null) {
            return false;
        }

        component.setCurrentKilometers(0.0);
        saveComponent(component);
        return true;
    }

    @Override
    @Transactional
    public boolean resetComponentsCurrentKilometers(Long bicycleId) {
        if (bicycleId == null) {
            return false;
        }
        
        Bicycle bicycle = bicycleService.findById(bicycleId);
        if (bicycle == null) {
            return false;
        }
        
        List<BicycleComponent> components = bicycle.getComponents();
        if (components == null || components.isEmpty()) {
            return true;
        }
        
        for (BicycleComponent component : components) {
            component.setCurrentKilometers(0.0);
            saveComponent(component);
        }
        
        return true;
    }

    @Override
    public Double getWearPercentage(Long componentId) {
        if (componentId == null) {
            return null;
        }
        
        BicycleComponent component = findById(componentId);
        if (component == null) {
            return 0.0;
        }
        
        if (component.getMaxKilometers() == null || component.getMaxKilometers() <= 0 || 
            component.getCurrentKilometers() == null) {
            return 0.0;
        }
        
        double percentage = (component.getCurrentKilometers() / component.getMaxKilometers()) * 100;
        return Math.min(100.0, Math.max(0.0, percentage));
    }

    @Override
    public Double getRemainingKilometers(Long componentId) {
        if (componentId == null) {
            return null;
        }
        
        BicycleComponent component = findById(componentId);
        if (component == null) {
            return 0.0;
        }
        
        if (component.getMaxKilometers() == null || component.getCurrentKilometers() == null) {
            return 0.0;
        }
        
        return Math.max(0.0, component.getMaxKilometers() - component.getCurrentKilometers());
    }

    @Override
    public Boolean needsMaintenance(Long componentId) {
        if (componentId == null) {
            return null;
        }
        
        BicycleComponent component = findById(componentId);
        if (component == null) {
            return null;
        }
        
        if (component.getMaxKilometers() == null || component.getCurrentKilometers() == null) {
            return false;
        }
        
        return component.getCurrentKilometers() >= component.getMaxKilometers();
    }

    @Override
    public List<BicycleComponent> getDefaultComponents(Bicycle bicycle) {
        if (bicycle == null) {
            return new ArrayList<>();
        }
        
        List<BicycleComponent> defaultComponents = new ArrayList<>();
        
        // Drive train components
        defaultComponents.add(new BicycleComponent(null, "Cadena", 2500.0, 0.0, bicycle));
        defaultComponents.add(new BicycleComponent(null, "Casete", 5000.0, 0.0, bicycle));
        defaultComponents.add(new BicycleComponent(null, "Platos", 10000.0, 0.0, bicycle));
        
        // Brake components
        defaultComponents.add(new BicycleComponent(null, "Pastillas de Freno Delanteras", 1500.0, 0.0, bicycle));
        defaultComponents.add(new BicycleComponent(null, "Pastillas de Freno Traseras", 1500.0, 0.0, bicycle));
        
        // Suspension components
        defaultComponents.add(new BicycleComponent(null, "Servicio de Horquilla", 3000.0, 0.0, bicycle));
        defaultComponents.add(new BicycleComponent(null, "Servicio de Amortiguador", 3000.0, 0.0, bicycle));
        
        // Wheels and tires
        defaultComponents.add(new BicycleComponent(null, "Neumático Delantero", 2000.0, 0.0, bicycle));
        defaultComponents.add(new BicycleComponent(null, "Neumático Trasero", 1500.0, 0.0, bicycle));
        defaultComponents.add(new BicycleComponent(null, "Rodamientos de Rueda Delantera", 8000.0, 0.0, bicycle));
        defaultComponents.add(new BicycleComponent(null, "Rodamientos de Rueda Trasera", 8000.0, 0.0, bicycle));
        
        // Other components
        defaultComponents.add(new BicycleComponent(null, "Pedalier", 5000.0, 0.0, bicycle));
        defaultComponents.add(new BicycleComponent(null, "Dirección", 10000.0, 0.0, bicycle));
        
        return defaultComponents;
    }
}
