package com.example.demo.dtos;

import com.example.demo.entities.User;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewUserDTO {
    private Long id;
    private String username;

    public ReviewUserDTO() {
    }

    public ReviewUserDTO(User user) {
        if (user != null) {
            this.id = user.getId();
            this.username = user.getUsername();
        }
    }

    public ReviewUserDTO(Long id, String username) {
        this.id = id;
        this.username = username;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}