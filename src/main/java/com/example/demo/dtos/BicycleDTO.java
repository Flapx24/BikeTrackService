package com.example.demo.dtos;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.example.demo.entities.Bicycle;
import com.example.demo.entities.BicycleComponent;
import com.example.demo.entities.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.constraints.NotBlank;

public class BicycleDTO {
    
    private Long id;
    
    @NotBlank(message = "El nombre de la bicicleta es obligatorio")
    private String name;
    
    private String iconUrl;
    
    private Long ownerId;
    
    private List<BicycleComponentDTO> components = new ArrayList<>();
    
    private Double totalKilometers = 0.0;
    
    private LocalDate lastMaintenanceDate;

    public BicycleDTO() {
    }

    public BicycleDTO(Bicycle bicycle) {
        if (bicycle != null) {
            this.id = bicycle.getId();
            this.name = bicycle.getName();
            this.iconUrl = bicycle.getIconUrl();
            this.ownerId = bicycle.getOwner() != null ? bicycle.getOwner().getId() : null;
            this.totalKilometers = bicycle.getTotalKilometers();
            this.lastMaintenanceDate = bicycle.getLastMaintenanceDate();
            
            if (bicycle.getComponents() != null) {
                this.components = bicycle.getComponents().stream()
                    .map(BicycleComponentDTO::new)
                    .collect(Collectors.toList());
            }
        }
    }
    
    /**
     * Converts this DTO to a Bicycle entity WITHOUT associated components.
     * IMPORTANT: This entity requires a valid User object before persisting.
     * 
     * @return Bicycle entity without components
     */
    public Bicycle toEntity() {
        Bicycle bicycle = new Bicycle();
        bicycle.setId(this.id);
        bicycle.setName(this.name);
        bicycle.setIconUrl(this.iconUrl);
        bicycle.setTotalKilometers(this.totalKilometers);
        bicycle.setLastMaintenanceDate(this.lastMaintenanceDate);
        return bicycle;
    }
    
    /**
     * Converts this DTO to a Bicycle entity with owner and components.
     * 
     * @param owner The user who owns this bicycle
     * @return Complete Bicycle entity with owner and components
     */
    public Bicycle toEntity(User owner) {
        Bicycle bicycle = toEntity();
        bicycle.setOwner(owner);
        
        if (this.components != null && !this.components.isEmpty()) {
            List<BicycleComponent> bicycleComponents = this.components.stream()
                .map(componentDTO -> componentDTO.toEntity(bicycle))
                .collect(Collectors.toList());
            bicycle.setComponents(bicycleComponents);
        }
        
        return bicycle;
    }
    
    public static BicycleDTO fromJson(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, BicycleDTO.class);
    }
    
    public String toJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public List<BicycleComponentDTO> getComponents() {
        return components;
    }

    public void setComponents(List<BicycleComponentDTO> components) {
        this.components = components;
    }

    public Double getTotalKilometers() {
        return totalKilometers;
    }

    public void setTotalKilometers(Double totalKilometers) {
        this.totalKilometers = totalKilometers;
    }

    public LocalDate getLastMaintenanceDate() {
        return lastMaintenanceDate;
    }

    public void setLastMaintenanceDate(LocalDate lastMaintenanceDate) {
        this.lastMaintenanceDate = lastMaintenanceDate;
    }
}