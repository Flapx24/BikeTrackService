package com.example.demo.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
public class Bicycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String iconUrl;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"password", "email", "active", "authorities"})
    private User owner;

    @OneToMany(mappedBy = "bicycle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BicycleComponent> components = new ArrayList<>();

    private Double totalKilometers = 0.0;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastMaintenanceDate;

    public Bicycle() {
    }

    public Bicycle(Long id, String name, String iconUrl, User owner, Double totalKilometers) {
        this.id = id;
        this.name = name;
        this.iconUrl = iconUrl;
        this.owner = owner;
        this.totalKilometers = totalKilometers != null ? totalKilometers : 0.0;
    }

    public Bicycle(Long id, String name, String iconUrl, User owner, Double totalKilometers, 
                   List<BicycleComponent> components) {
        this.id = id;
        this.name = name;
        this.iconUrl = iconUrl;
        this.owner = owner;
        this.totalKilometers = totalKilometers != null ? totalKilometers : 0.0;
        
        if (components != null) {
            this.components = components;
            for (BicycleComponent component : components) {
                component.setBicycle(this);
            }
        }
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

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public List<BicycleComponent> getComponents() {
        return components;
    }

    public void setComponents(List<BicycleComponent> components) {
        this.components.clear();
        if (components != null) {
            this.components.addAll(components);
            for (BicycleComponent component : components) {
                component.setBicycle(this);
            }
        }
    }

    public Double getTotalKilometers() {
        return totalKilometers;
    }

    public void setTotalKilometers(Double totalKilometers) {
        this.totalKilometers = totalKilometers;
    }

    public Date getLastMaintenanceDate() {
        return lastMaintenanceDate;
    }

    public void setLastMaintenanceDate(Date lastMaintenanceDate) {
        this.lastMaintenanceDate = lastMaintenanceDate;
    }

    @Override
    public String toString() {
        return "Bicycle [id=" + id + ", name=" + name + ", owner=" + (owner != null ? owner.getUsername() : "null") + 
               ", totalKilometers=" + totalKilometers + ", components=" + components.size() + "]";
    }
}
