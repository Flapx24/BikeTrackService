package com.example.demo.dtos;

import com.example.demo.entities.User;
import com.example.demo.enums.Role;

public class UserDTO {
    private Long id;
    private String name;
    private String username;
    private String email;
    private String surname;
    private String imageUrl;
    private String role;
    private Boolean active;

    public UserDTO() {
    }

    public UserDTO(User user) {
        if (user != null) {
            this.id = user.getId();
            this.name = user.getName();
            this.username = user.getUsername();
            this.email = user.getEmail();
            this.surname = user.getSurname();
            this.imageUrl = user.getImageUrl();
            this.role = user.getRole() != null ? user.getRole().name() : null;
            this.active = user.getActive();
        }
    }

    public UserDTO(Long id, String username) {
        this.id = id;
        this.username = username;
    }
    
    public User toEntity() {
        User user = new User();
        user.setId(this.id);
        user.setUsername(this.username);
        user.setName(this.name);
        user.setSurname(this.surname);
        user.setEmail(this.email);
        user.setImageUrl(this.imageUrl);
        if (this.role != null) {
            user.setRole(Role.valueOf(this.role));
        }
        user.setActive(this.active);
        return user;
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
    
    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
    
    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "UserDTO [id=" + id + ", name=" + name + ", username=" + username +
                ", email=" + email + ", surname=" + surname + ", imageUrl=" + imageUrl + 
                ", role=" + role + ", active=" + active + "]";
    }
}