package com.example.demo.entities;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Route {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	private String description;

	@Enumerated(EnumType.STRING)
	private Difficulty difficulty;

	private List<String> imageUrls;

	private List<Review> reviews;

	private double reviewsScoreAverage;

	private String city;
	
	@Column(nullable = false)
	private List<String> coordinates;

	public Route() {
		super();
	}

	public Route(Long id, String title, String description, Difficulty difficulty, List<String> imageUrls,
			List<Review> reviews, double reviewsScoreAverage, List<String> coordinates) {
		super();
		this.id = id;
		this.title = title;
		this.description = description;
		this.difficulty = difficulty;
		this.imageUrls = imageUrls;
		this.reviews = reviews;
		this.reviewsScoreAverage = reviewsScoreAverage;
		this.coordinates = coordinates;
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

	public List<Review> getReviews() {
		return reviews;
	}

	public void setReviews(List<Review> reviews) {
		this.reviews = reviews;
	}

	public double getReviewsScoreAverage() {
		return reviewsScoreAverage;
	}

	public void setReviewsScoreAverage(double reviewsScoreAverage) {
		this.reviewsScoreAverage = reviewsScoreAverage;
	}

	public List<String> getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(List<String> coordinates) {
		this.coordinates = coordinates;
	}

	@Override
	public String toString() {
		return "Route [id=" + id + ", title=" + title + ", description=" + description + ", difficulty=" + difficulty
				+ ", imageUrls=" + imageUrls + ", reviews=" + reviews + ", reviewsScoreAverage=" + reviewsScoreAverage
				+ ", coordinates=" + coordinates + "]";
	}

}
