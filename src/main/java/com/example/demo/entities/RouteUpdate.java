package com.example.demo.entities;

import java.time.LocalDate;

import com.example.demo.enums.UpdateType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class RouteUpdate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDate date;
    
    @Enumerated(EnumType.STRING)
    private UpdateType type;

    @Column(nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean isResolved;

    @ManyToOne
    @JoinColumn(name = "route_id")
    private Route route;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public RouteUpdate() {
        super();
    }

    public RouteUpdate(Long id, String description, LocalDate date, UpdateType type, boolean isResolved, Route route,
            User user) {
        super();
        this.id = id;
        this.description = description;
        this.date = date;
        this.type = type;
        this.isResolved = isResolved;
        this.route = route;
        this.user = user;
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
        return isResolved;
    }

    public void setResolved(boolean isResolved) {
        this.isResolved = isResolved;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "RouteUpdate [id=" + id + ", description=" + description + ", date=" + date +
                ", type=" + type + ", isResolved=" + isResolved + ", route=" + route + ", user=" + user + "]";
    }
}
