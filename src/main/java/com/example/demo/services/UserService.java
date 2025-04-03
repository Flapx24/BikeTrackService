package com.example.demo.services;

import com.example.demo.entities.User;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    User saveUser(User user);

    boolean existsByEmail(String email);

    User findByEmail(String email);

	void setToken(User user);
    
    List<User> getAllUsers();
    
    List<String> findEmailsByQuery(String query);
}

