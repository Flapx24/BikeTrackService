package com.example.demo.dtos;

import java.util.ArrayList;
import java.util.List;

import com.example.demo.entities.Workshop;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public class WorkshopDTO {

    private Long id;

    @NotBlank(message = "Workshop name is required")
    private String name;

    private List<String> imageUrls = new ArrayList<>();

    @NotEmpty(message = "At least one coordinate is required")
    @Size(min = 2, message = "At least latitude and longitude coordinates are required")
    private List<String> coordinates = new ArrayList<>();

    public WorkshopDTO() {
    }

    public WorkshopDTO(Workshop workshop) {
        if (workshop != null) {
            this.id = workshop.getId();
            this.name = workshop.getName();

            if (workshop.getImageUrls() != null) {
                this.imageUrls = new ArrayList<>(workshop.getImageUrls());
            }

            if (workshop.getCoordinates() != null) {
                this.coordinates = new ArrayList<>(workshop.getCoordinates());
            }
        }
    }

    public Workshop toEntity() {
        Workshop workshop = new Workshop();
        workshop.setId(this.id);
        workshop.setName(this.name);
        workshop.setImageUrls(this.imageUrls);
        workshop.setCoordinates(this.coordinates);
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

    public List<String> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<String> coordinates) {
        this.coordinates = coordinates;
    }

    @Override
    public String toString() {
        return "WorkshopDTO [id=" + id + ", name=" + name +
                ", imageUrls=" + imageUrls + ", coordinates=" + coordinates + "]";
    }
}
