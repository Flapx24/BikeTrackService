package com.example.demo.dtos;

import java.time.LocalDate;

import com.example.demo.entities.RouteUpdate;
import com.example.demo.enums.UpdateType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RouteUpdateDTO {

    private Long id;

    @NotBlank(message = "La descripción es obligatoria")
    private String description;

    @NotNull(message = "La fecha es obligatoria")
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @NotNull(message = "El tipo de actualización es obligatorio")
    private UpdateType type;

    private boolean resolved;

    private Long routeId;

    public RouteUpdateDTO() {
    }

    public RouteUpdateDTO(RouteUpdate update) {
        if (update != null) {
            this.id = update.getId();
            this.description = update.getDescription();
            this.date = update.getDate();
            this.type = update.getType();
            this.resolved = update.isResolved();

            if (update.getRoute() != null) {
                this.routeId = update.getRoute().getId();
            }
        }
    }

    /**
     * Converts this DTO to a RouteUpdate entity WITHOUT associated Route.
     * IMPORTANT: This entity requires a valid Route object before persisting.
     * 
     * @return RouteUpdate entity without route
     */
    public RouteUpdate toEntity() {
        RouteUpdate update = new RouteUpdate();
        update.setId(this.id);
        update.setDescription(this.description);
        update.setDate(this.date != null ? this.date : LocalDate.now());
        update.setType(this.type);
        update.setResolved(this.resolved);
        return update;
    }

    public static RouteUpdateDTO fromJson(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, RouteUpdateDTO.class);
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public UpdateType getType() {
        return type;
    }

    public void setType(UpdateType type) {
        this.type = type;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }
}