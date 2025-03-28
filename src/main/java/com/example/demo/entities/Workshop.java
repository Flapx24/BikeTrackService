package com.example.demo.entities;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Workshop {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@ElementCollection
	private List<String> imageUrls;

	@ElementCollection
	@Column(nullable = false)
	private List<String> coordinates;

	public Workshop() {
		super();
	}

	public Workshop(Long id, String name, List<String> imageUrls, List<String> coordinates) {
		super();
		this.id = id;
		this.name = name;
		this.imageUrls = imageUrls;
		this.coordinates = coordinates;
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

	public List<String> getImageUrls() {
		return imageUrls;
	}

	public void setImageUrls(List<String> imageUrls) {
		this.imageUrls = imageUrls;
	}

	public List<String> getCoordinates() {
		return coordinates;
	}

	public void setCoordinates(List<String> coordinates) {
		this.coordinates = coordinates;
	}

	@Override
	public String toString() {
		return "Workshop [id=" + id + ", name=" + name + ", imageUrls=" + imageUrls + ", coordinates=" + coordinates
				+ "]";
	}

}
