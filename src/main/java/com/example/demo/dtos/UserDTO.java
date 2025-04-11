package com.example.demo.dtos;

import com.example.demo.entities.User;

public class UserDTO {
    private Long id;
    private String name;
    private String username;
    private String email;
    private String role;

    public UserDTO() {
    }

    public UserDTO(User user) {
        if (user != null) {
            this.id = user.getId();
            this.name = user.getName();
            this.username = user.getUsername();
            this.email = user.getEmail();
            this.role = user.getRole() != null ? user.getRole().name() : null;
        }
    }

    public UserDTO(Long id, String username) {
        this.id = id;
        this.username = username;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String toString() {
        return "UserDTO [id=" + id + ", name=" + name + ", username=" + username +
                ", email=" + email + ", role=" + role + "]";
    }
}