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
    
    @NotBlank(message = "The component name is required")
    private String name;
    
    @NotNull(message = "The maximum kilometers is required")
    @Positive(message = "The maximum kilometers must be greater than zero")
    private Double maxKilometers;
    
    @NotNull(message = "The current kilometers is required")
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
    
    /**
     * Creates a BicycleComponentDTO from a JSON string
     * 
     * @param json String in JSON format
     * @return BicycleComponentDTO object
     * @throws JsonProcessingException If an error occurs while processing the JSON
     */
    public static BicycleComponentDTO fromJson(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, BicycleComponentDTO.class);
    }
    
    /**
     * Converts this object to a JSON string
     * 
     * @return String in JSON format
     * @throws JsonProcessingException If an error occurs while generating the JSON
     */
    public String toJson() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }
    
    // Getters and setters
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
