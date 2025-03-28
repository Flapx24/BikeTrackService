package com.example.demo.entities;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Incidents {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String description;

	@Column(nullable = false)
	private LocalDate date;

	@Column(nullable = false)
	private boolean isResolved;
	
	@ManyToOne
	@JoinColumn(name = "route_id")
	private Route route;

	public Incidents() {
		super();
	}

	public Incidents(Long id, String description, LocalDate date, boolean isResolved) {
		super();
		this.id = id;
		this.description = description;
		this.date = date;
		this.isResolved = isResolved;
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

	public boolean isResolved() {
		return isResolved;
	}

	public void setResolved(boolean isResolved) {
		this.isResolved = isResolved;
	}

	@Override
	public String toString() {
		return "Incidents [id=" + id + ", description=" + description + ", date=" + date + ", isResolved=" + isResolved
				+ "]";
	}

}
