package com.example.demo.dtos;

import java.time.LocalDate;

import com.example.demo.entities.Review;
import com.example.demo.entities.Route;
import com.example.demo.entities.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewDTO {

    private Long id;

    private ReviewUserDTO user;

    @NotNull(message = "La calificación es obligatoria")
    @Min(value = 1, message = "La calificación debe estar entre 1 y 5")
    @Max(value = 5, message = "La calificación debe estar entre 1 y 5")
    private Integer rating;

    private String text;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    private LocalDate date;

    private Long routeId;

    public ReviewDTO() {
    }

    public ReviewDTO(Review review) {
        if (review != null) {
            this.id = review.getId();
            if (review.getUser() != null) {
                this.user = new ReviewUserDTO(
                        review.getUser().getId(),
                        review.getUser().getUsername());
            }
            this.rating = review.getRating();
            this.text = review.getText();
            this.date = review.getDate();
            if (review.getRoute() != null) {
                this.routeId = review.getRoute().getId();
            }
        }
    }

    /**
     * Converts this DTO to a Review entity WITHOUT associated User and Route.
     * IMPORTANT: This entity requires valid User and Route objects before
     * persisting.
     * 
     * @return Review entity without relationships
     */
    public Review toEntity() {
        Review review = new Review();
        review.setId(this.id);
        review.setRating(this.rating);
        review.setText(this.text);
        review.setDate(this.date != null ? this.date : LocalDate.now());
        return review;
    }

    /**
     * Converts this DTO to a Review entity with User and Route relationships.
     * 
     * @param user  The user who wrote this review
     * @param route The route being reviewed
     * @return Complete Review entity with relationships
     */
    public Review toEntity(User user, Route route) {
        Review review = toEntity();
        review.setUser(user);
        review.setRoute(route);
        return review;
    }

    public static ReviewDTO fromJson(String json) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, ReviewDTO.class);
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

    public ReviewUserDTO getUser() {
        return user;
    }

    public void setUser(ReviewUserDTO user) {
        this.user = user;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
    }
}
