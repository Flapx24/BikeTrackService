package com.example.demo.dtos;

import java.time.LocalDate;

import com.example.demo.entities.Bicycle;

/**
 * Simplified DTO for bicycle list views.
 * Contains only essential information without components.
 */
public class BicycleSummaryDTO {

    private Long id;
    private String name;
    private String iconUrl;
    private Long ownerId;
    private Double totalKilometers;
    private LocalDate lastMaintenanceDate;
    private Boolean needsMaintenance;
    private Integer componentCount;

    public BicycleSummaryDTO() {
    }

    public BicycleSummaryDTO(Bicycle bicycle) {
        if (bicycle != null) {
            this.id = bicycle.getId();
            this.name = bicycle.getName();
            this.iconUrl = bicycle.getIconUrl();
            this.ownerId = bicycle.getOwner() != null ? bicycle.getOwner().getId() : null;
            this.totalKilometers = bicycle.getTotalKilometers();
            this.lastMaintenanceDate = bicycle.getLastMaintenanceDate();
            // Calculate component count and check if maintenance is needed
            if (bicycle.getComponents() != null) {
                this.componentCount = bicycle.getComponents().size();

                // Check if any component needs maintenance - stop at first match for efficiency
                this.needsMaintenance = false;
                for (var component : bicycle.getComponents()) {
                    if (component.getMaxKilometers() != null && component.getCurrentKilometers() != null) {
                        if (component.getCurrentKilometers() >= component.getMaxKilometers()) {
                            this.needsMaintenance = true;
                            break;
                        }
                    }
                }
            } else {
                this.componentCount = 0;
                this.needsMaintenance = false;
            }
        }
    }

    // Getters and Setters
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

    public Boolean getNeedsMaintenance() {
        return needsMaintenance;
    }

    public void setNeedsMaintenance(Boolean needsMaintenance) {
        this.needsMaintenance = needsMaintenance;
    }

    public Integer getComponentCount() {
        return componentCount;
    }

    public void setComponentCount(Integer componentCount) {
        this.componentCount = componentCount;
    }
}
