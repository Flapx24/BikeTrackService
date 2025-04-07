package com.example.demo.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class BicycleComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double maxKilometers;

    @Column(nullable = false)
    private Double currentKilometers;

    @ManyToOne
    @JoinColumn(name = "bicycle_id")
    @JsonIgnore
    private Bicycle bicycle;

    public BicycleComponent() {
    }

    public BicycleComponent(Long id, String name, Double maxKilometers, Double currentKilometers, Bicycle bicycle) {
        this.id = id;
        this.name = name;
        this.maxKilometers = maxKilometers;
        this.currentKilometers = currentKilometers != null ? currentKilometers : 0.0;
        this.bicycle = bicycle;
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

    public Bicycle getBicycle() {
        return bicycle;
    }

    public void setBicycle(Bicycle bicycle) {
        this.bicycle = bicycle;
    }

    @Override
    public String toString() {
        return "BicycleComponent [id=" + id + ", name=" + name + ", maxKilometers=" + maxKilometers
                + ", currentKilometers=" + currentKilometers + "]";
    }

}
