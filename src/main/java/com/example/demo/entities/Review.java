package com.example.demo.entities;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Entity
@Table(uniqueConstraints = {
		@UniqueConstraint(columnNames = { "user_id", "route_id" })
})
public class Review {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false)
	@Min(value = 1, message = "La calificación debe estar entre 1 y 5")
	@Max(value = 5, message = "La calificación debe estar entre 1 y 5")
	private Integer rating;

	private String text;

	@Column(nullable = false)
	private LocalDate date;

	@ManyToOne
	@JoinColumn(name = "route_id")
	private Route route;

	public Review() {
	}

	public Review(Long id, User user,
			@Min(value = 1, message = "La calificación debe estar entre 1 y 5") @Max(value = 5, message = "La calificación debe estar entre 1 y 5") Integer rating,
			String text, LocalDate date, Route route) {
		this.id = id;
		this.user = user;
		this.rating = rating;
		this.text = text;
		this.date = date;
		this.route = route;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
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

	public Route getRoute() {
		return route;
	}

	public void setRoute(Route route) {
		this.route = route;
	}

	@Override
	public String toString() {
		return "Review [id=" + id + ", user=" + user + ", rating=" + rating + ", text=" + text + ", date=" + date
				+ ", route=" + route + "]";
	}

}
