package com.example.demo.dtos;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.example.demo.entities.Review;
import com.example.demo.entities.Route;
import com.example.demo.entities.RouteUpdate;
import com.example.demo.enums.Difficulty;
import com.example.demo.enums.RouteDetailLevel;
import com.example.demo.models.GeoPoint;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RouteDTO {
    private Long id;

    @NotBlank(message = "El título de la ruta es obligatorio")
    private String title;

    private String description;

    @NotNull(message = "La dificultad es obligatoria")
    private String difficulty;

    private List<String> imageUrls = new ArrayList<>();

    @NotBlank(message = "La ciudad es obligatoria")
    private String city;

    @Valid
    @Size(min = 1, message = "Al menos un punto de ruta es necesario")
    @NotEmpty(message = "Los puntos de ruta son obligatorios")
    private List<GeoPoint> routePoints = new ArrayList<>();
    private Double averageReviewScore = 0.0;

    private List<ReviewDTO> reviews = new ArrayList<>();

    private List<RouteUpdateDTO> updates = new ArrayList<>();

    private Integer reviewCount;

    private Integer updateCount;

    private List<GeoPoint> calculatedRoutePoints = new ArrayList<>();
    private Integer calculatedEstimatedTimeMinutes;
    private Double calculatedTotalDistanceKm;

    public RouteDTO() {
    }

    public RouteDTO(String title, String description, String difficulty, List<String> imageUrls, String city,
            List<GeoPoint> routePoints) {
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
        this.city = city;
        this.routePoints = routePoints != null ? routePoints : new ArrayList<>();
    }

    public RouteDTO(Long id, String title, String description, String difficulty, List<String> imageUrls, String city,
            List<GeoPoint> routePoints, double averageReviewScore, List<ReviewDTO> reviews,
            List<RouteUpdateDTO> updates) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.difficulty = difficulty;
        this.imageUrls = imageUrls != null ? imageUrls : new ArrayList<>();
        this.city = city;
        this.routePoints = routePoints != null ? routePoints : new ArrayList<>();
        this.averageReviewScore = averageReviewScore;
        this.reviews = reviews != null ? reviews : new ArrayList<>();
        this.updates = updates != null ? updates : new ArrayList<>();
    }

    /**
     * Converts a Route entity to a RouteDTO with different levels of detail
     * 
     * @param route       The entity to convert
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
        dto.setAverageReviewScore(route.getAverageReviewScore() != null ? route.getAverageReviewScore() : 0.0);
        dto.setDescription(route.getDescription());
        dto.setCalculatedRoutePoints(route.getCalculatedRoutePoints());
        dto.setCalculatedEstimatedTimeMinutes(route.getCalculatedEstimatedTimeMinutes());
        dto.setCalculatedTotalDistanceKm(route.getCalculatedTotalDistanceKm());

        if (detailLevel == RouteDetailLevel.FULL) {
            dto.setRoutePoints(route.getRoutePoints());

            if (route.getReviews() != null) {
                dto.setReviews(route.getReviews().stream()
                        .map(ReviewDTO::new)
                        .collect(Collectors.toList()));
            } else {
                dto.setReviews(new ArrayList<>());
            }

            if (route.getUpdates() != null) {
                dto.setUpdates(route.getUpdates().stream()
                        .map(RouteUpdateDTO::new)
                        .collect(Collectors.toList()));
            } else {
                dto.setUpdates(new ArrayList<>());
            }
        } else {
            dto.setReviews(null);
            dto.setUpdates(null);
            dto.setRoutePoints(null);
        }

        return dto;
    }

    /**
     * Converts this DTO to a complete Route entity ready for persistence.
     * This method properly handles the conversion of related entities for creating
     * and updating routes.
     * 
     * @return Complete Route entity with proper relationships
     */
    public Route toEntity() {
        return toEntity(null);
    }

    /**
     * Converts this DTO to a Route entity, using an existing entity if provided.
     * This preserves relationships with reviews and updates when updating an
     * existing route.
     * 
     * @param existingRoute Optional existing route entity to update (null for new
     *                      routes)
     * @return Route entity with preserved relationships
     */
    public Route toEntity(Route existingRoute) {
        Route route = existingRoute != null ? existingRoute : new Route();
        route.setId(this.id);
        route.setTitle(this.title);
        route.setDescription(this.description);
        route.setDifficulty(this.difficulty == null ? Difficulty.EASY : Difficulty.valueOf(this.difficulty));
        route.setImageUrls(this.imageUrls);
        route.setCity(this.city);
        route.setRoutePoints(this.routePoints);
        route.setCalculatedRoutePoints(this.calculatedRoutePoints);
        route.setCalculatedEstimatedTimeMinutes(this.calculatedEstimatedTimeMinutes);
        route.setCalculatedTotalDistanceKm(this.calculatedTotalDistanceKm);

        if (existingRoute != null) {

            route.setAverageReviewScore(
                    existingRoute.getAverageReviewScore() != null ? existingRoute.getAverageReviewScore() : 0.0);

            if (existingRoute.getReviews() != null) {
                route.setReviews(existingRoute.getReviews());
            }

            if (existingRoute.getUpdates() != null) {
                route.setUpdates(existingRoute.getUpdates());
            }
        } else {
            route.setAverageReviewScore(this.averageReviewScore != null ? this.averageReviewScore : 0.0);

            if (this.reviews != null && !this.reviews.isEmpty()) {
                List<Review> reviewEntities = new ArrayList<>();
                for (ReviewDTO reviewDTO : this.reviews) {
                    Review review = reviewDTO.toEntity();
                    review.setRoute(route);
                    reviewEntities.add(review);
                }
                route.setReviews(reviewEntities);
            }

            if (this.updates != null && !this.updates.isEmpty()) {
                List<RouteUpdate> updateEntities = new ArrayList<>();
                for (RouteUpdateDTO updateDTO : this.updates) {
                    RouteUpdate update = updateDTO.toEntity();
                    update.setRoute(route);
                    updateEntities.add(update);
                }
                route.setUpdates(updateEntities);
            }
        }

        return route;
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

    public List<GeoPoint> getRoutePoints() {
        return routePoints;
    }

    public void setRoutePoints(List<GeoPoint> routePoints) {
        this.routePoints = routePoints;
    }

    public Double getAverageReviewScore() {
        return averageReviewScore != null ? averageReviewScore : 0.0;
    }

    public void setAverageReviewScore(Double averageReviewScore) {
        this.averageReviewScore = averageReviewScore != null ? averageReviewScore : 0.0;
    }

    public List<ReviewDTO> getReviews() {
        return reviews;
    }

    public void setReviews(List<ReviewDTO> reviews) {
        this.reviews = reviews;
    }

    public List<RouteUpdateDTO> getUpdates() {
        return updates;
    }

    public void setUpdates(List<RouteUpdateDTO> updates) {
        this.updates = updates;
    }

    public Integer getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Integer reviewCount) {
        this.reviewCount = reviewCount;
    }

    public Integer getUpdateCount() {
        return updateCount;
    }

    public void setUpdateCount(Integer updateCount) {
        this.updateCount = updateCount;
    }

    public List<GeoPoint> getCalculatedRoutePoints() {
        return calculatedRoutePoints;
    }

    public void setCalculatedRoutePoints(List<GeoPoint> calculatedRoutePoints) {
        this.calculatedRoutePoints = calculatedRoutePoints;
    }    public Integer getCalculatedEstimatedTimeMinutes() {
        return calculatedEstimatedTimeMinutes;
    }

    public void setCalculatedEstimatedTimeMinutes(Integer calculatedEstimatedTimeMinutes) {
        this.calculatedEstimatedTimeMinutes = calculatedEstimatedTimeMinutes;
    }

    public Double getCalculatedTotalDistanceKm() {
        return calculatedTotalDistanceKm;
    }

    public void setCalculatedTotalDistanceKm(Double calculatedTotalDistanceKm) {
        this.calculatedTotalDistanceKm = calculatedTotalDistanceKm;
    }

    @Override
    public String toString() {
        return "RouteDTO [id=" + id + ", title=" + title + ", description=" + description + ", difficulty=" + difficulty
                + ", imageUrls=" + imageUrls + ", city=" + city + ", routePoints=" + routePoints
                + ", averageReviewScore=" + averageReviewScore + ", reviews=" + reviews + ", updates=" + updates
                + ", reviewCount=" + reviewCount + ", updateCount=" + updateCount + ", calculatedRoutePoints="
                + calculatedRoutePoints + ", calculatedEstimatedTimeMinutes=" + calculatedEstimatedTimeMinutes
                + ", calculatedTotalDistanceKm=" + calculatedTotalDistanceKm + "]";
    }

}
