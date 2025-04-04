package com.example.demo.entities;

import java.util.ArrayList;
import java.util.List;

import com.example.demo.enums.Difficulty;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class Route {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Difficulty difficulty;

	@ElementCollection
	private List<String> imageUrls = new ArrayList<>();

	@Column(nullable = false)
	private String city;

	@Column(nullable = false)
	@ElementCollection
	private List<String> coordinates = new ArrayList<>();

	private Double averageReviewScore = 0.0;

	@OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Review> reviews = new ArrayList<>();

	@OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<RouteUpdate> updates = new ArrayList<>();

	public Route() {
	}

	public Route(Long id, String title, String description, Difficulty difficulty, List<String> imageUrls, String city,
			List<String> coordinates, Double averageReviewScore, List<Review> reviews, List<RouteUpdate> updates) {
		this.id = id;
		this.title = title;
		this.description = description;
		this.difficulty = difficulty == null ? Difficulty.EASY : difficulty;
		this.imageUrls = imageUrls;
		this.city = city;
		this.coordinates = coordinates;
		this.averageReviewScore = averageReviewScore == null ? 0.0 : averageReviewScore;
		this.reviews = reviews == null ? new ArrayList<>() : reviews;
		this.updates = updates == null ? new ArrayList<>() : updates;
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

	public Difficulty getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(Difficulty difficulty) {
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
		return "Route [id=" + id + ", title=" + title + ", description=" + description + ", difficulty=" + difficulty
				+ ", imageUrls=" + imageUrls + ", city=" + city + ", coordinates=" + coordinates
				+ ", averageReviewScore=" + averageReviewScore + ", reviews=" + reviews + ", updates=" + updates
				+ "]";
	}

}
