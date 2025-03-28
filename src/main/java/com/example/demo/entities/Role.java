package com.example.demo.entities;

public enum Role {
	ROLE_ADMIN, ROLE_USER;

	@Override
    public String toString() {
        return name().substring(5); 
    }
}
