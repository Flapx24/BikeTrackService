package com.example.demo.entities;

import java.util.ArrayList;
import java.util.List;

import com.example.demo.models.GeoPoint;

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
	private List<String> imageUrls = new ArrayList<>();

	@Column(nullable = false)
	private String city;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String address;
	
	@Column(nullable = false, columnDefinition = "TEXT")
	private String coordinates;

	public Workshop() {
		super();
	}

	public Workshop(Long id, String name, List<String> imageUrls, String city, String address, GeoPoint coordinates) {
		super();
		this.id = id;
		this.name = name;
		this.imageUrls = imageUrls;
		this.city = city;
		this.address = address;
		setCoordinates(coordinates);
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

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public List<String> getImageUrls() {
		return imageUrls;
	}

	public void setImageUrls(List<String> imageUrls) {
		this.imageUrls = imageUrls;
	}
	
	public String getAddress() {
		return address;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public GeoPoint getCoordinates() {
		return coordinates != null ? GeoPoint.fromString(coordinates) : null;
	}
	
	public void setCoordinates(GeoPoint coordinates) {
		this.coordinates = coordinates != null ? coordinates.toString() : null;
	}

	@Override
	public String toString() {
		return "Workshop [id=" + id + ", name=" + name + ", city=" + city + ", imageUrls=" + imageUrls + 
				", address=" + address + ", coordinates=" + getCoordinates() + "]";
	}
}
