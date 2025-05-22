package com.example.demo.services;

import com.example.demo.entities.User;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    User saveUser(User user);

    User findById(Long id);

    User findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> getAllUsers();

    List<User> findByUsername(String username);

    List<User> findByEmailIgnoreCase(String email);

}
