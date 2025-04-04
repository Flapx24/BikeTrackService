package com.example.demo.dtos;

import java.util.ArrayList;
import java.util.List;

import com.example.demo.entities.Review;
import com.example.demo.entities.Route;
import com.example.demo.entities.RouteUpdate;
import com.example.demo.enums.Difficulty;
import com.example.demo.enums.RouteDetailLevel;

public class RouteDTO {
    private Long id;
    private String title;
    private String description;
    private String difficulty;
    private List<String> imageUrls = new ArrayList<>();
    private String city;
    private List<String> coordinates = new ArrayList<>();
    private Double averageReviewScore;
    private List<Review> reviews = new ArrayList<>();
    private List<RouteUpdate> updates = new ArrayList<>();

    public RouteDTO() {
    }

    public RouteDTO(String title, String description, String difficulty, List<String> imageUrls, String city,
            List<String> coordinates) {
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
        this.city = city;
        this.coordinates = coordinates != null ? coordinates : new ArrayList<>();
    }

    public RouteDTO(Long id, String title, String description, String difficulty, List<String> imageUrls, String city,
            List<String> coordinates, double averageReviewScore, List<Review> reviews, List<RouteUpdate> updates) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
        this.city = city;
        this.coordinates = coordinates != null ? coordinates : new ArrayList<>();
        this.averageReviewScore = averageReviewScore;
        this.reviews = reviews != null ? reviews : new ArrayList<>();
        this.updates = updates != null ? updates : new ArrayList<>();
    }

    /**
     * Converts a Route entity to a RouteDTO with different levels of detail
     * 
     * @param route The entity to convert
     * @param detailLevel Detail level: BASIC or FULL
     * @return A DTO with the requested level of detail
     */
    public static RouteDTO fromEntity(Route route, RouteDetailLevel detailLevel) {
        if (route == null)
            return null;

        RouteDTO dto = new RouteDTO();
        dto.setId(route.getId());
        dto.setTitle(route.getTitle());
        dto.setDifficulty(route.getDifficulty().name());
        dto.setImageUrls(route.getImageUrls());
        dto.setCity(route.getCity());
        dto.setAverageReviewScore(route.getAverageReviewScore());

        if (detailLevel == RouteDetailLevel.FULL) {
            dto.setDescription(route.getDescription());
            dto.setCoordinates(route.getCoordinates());
            dto.setReviews(route.getReviews());
            dto.setUpdates(route.getUpdates());
        }

        return dto;
    }

    public Route toEntity() {
        return new Route(this.id, this.title, this.description,
                this.difficulty == null ? Difficulty.EASY : Difficulty.valueOf(this.difficulty), this.imageUrls,
                this.city, this.coordinates, this.averageReviewScore, this.reviews, this.updates);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public List<String> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<String> coordinates) {
        this.coordinates = coordinates;
    }

    public Double getAverageReviewScore() {
        return averageReviewScore;
    }

    public void setAverageReviewScore(Double averageReviewScore) {
        this.averageReviewScore = averageReviewScore;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public List<RouteUpdate> getUpdates() {
        return updates;
    }

    public void setUpdates(List<RouteUpdate> updates) {
        this.updates = updates;
    }

    @Override
    public String toString() {
        return "RouteDTO [id=" + id + ", title=" + title + ", description=" + description + ", difficulty=" + difficulty
                + ", imageUrls=" + imageUrls + ", city=" + city + ", coordinates=" + coordinates
                + ", averageReviewScore=" + averageReviewScore + ", reviews=" + reviews + ", updates=" + updates
                + "]";
    }
}
