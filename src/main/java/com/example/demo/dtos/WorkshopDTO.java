package com.example.demo.dtos;

import java.util.ArrayList;
import java.util.List;

import com.example.demo.entities.Workshop;
import com.example.demo.models.GeoPoint;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class WorkshopDTO {

    private Long id;

    @NotBlank(message = "Workshop name is required")
    private String name;

    private List<String> imageUrls = new ArrayList<>();

    @Valid
    @NotNull(message = "Location is required")
    private GeoPoint location;

    public WorkshopDTO() {
    }

    public WorkshopDTO(Workshop workshop) {
        if (workshop != null) {
            this.id = workshop.getId();
            this.name = workshop.getName();
            this.location = workshop.getLocation();

            if (workshop.getImageUrls() != null) {
                this.imageUrls = new ArrayList<>(workshop.getImageUrls());
            }
        }
    }

    public Workshop toEntity() {
        Workshop workshop = new Workshop();
        workshop.setId(this.id);
        workshop.setName(this.name);
        workshop.setImageUrls(this.imageUrls);
        workshop.setLocation(this.location);
        return workshop;
    }

    public static WorkshopDTO fromJson(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, WorkshopDTO.class);
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

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "WorkshopDTO [id=" + id + ", name=" + name + 
                ", imageUrls=" + imageUrls + ", location=" + location + "]";
    }
}
