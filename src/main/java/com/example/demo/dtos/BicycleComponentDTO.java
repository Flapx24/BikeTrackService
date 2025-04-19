package com.example.demo.dtos;

import com.example.demo.entities.Bicycle;
import com.example.demo.entities.BicycleComponent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class BicycleComponentDTO {
    
    private Long id;
    
    @NotBlank(message = "El nombre del componente es obligatorio")
    private String name;
    
    @NotNull(message = "El número máximo de kilómetros es obligatorio")
    @Positive(message = "El número máximo de kilómetros debe ser mayor que cero")
    private Double maxKilometers;
    
    @NotNull(message = "Los kilómetros actuales son obligatorios")
    private Double currentKilometers;

    public BicycleComponentDTO() {
    }

    public BicycleComponentDTO(BicycleComponent component) {
        if (component != null) {
            this.id = component.getId();
            this.name = component.getName();
            this.maxKilometers = component.getMaxKilometers();
            this.currentKilometers = component.getCurrentKilometers();
        }
    }
    
    /**
     * Converts this DTO to a BicycleComponent entity WITHOUT an associated bicycle.
     * IMPORTANT: This entity should not be persisted without first assigning a bicycle.
     * Use toEntity(Bicycle bicycle) to create a complete entity.
     * 
     * @return BicycleComponent entity without an associated bicycle
     */
    public BicycleComponent toEntity() {
        BicycleComponent component = new BicycleComponent();
        component.setId(this.id);
        component.setName(this.name);
        component.setMaxKilometers(this.maxKilometers);
        component.setCurrentKilometers(this.currentKilometers);
        return component;
    }
    
    /**
     * Converts this DTO to a BicycleComponent entity with an associated bicycle.
     * 
     * @param bicycle The bicycle to associate with this component
     * @return Complete BicycleComponent entity with associated bicycle
     */
    public BicycleComponent toEntity(Bicycle bicycle) {
        BicycleComponent component = toEntity();
        component.setBicycle(bicycle);
        return component;
    }
    
    public static BicycleComponentDTO fromJson(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, BicycleComponentDTO.class);
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

    public Double getMaxKilometers() {
        return maxKilometers;
    }

    public void setMaxKilometers(Double maxKilometers) {
        this.maxKilometers = maxKilometers;
    }

    public Double getCurrentKilometers() {
        return currentKilometers;
    }

    public void setCurrentKilometers(Double currentKilometers) {
        this.currentKilometers = currentKilometers;
    }
}
