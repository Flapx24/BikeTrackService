package com.example.demo.services;

import com.example.demo.entities.User;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {

    User saveUser(User user);

    User findById(Long id);

    User findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<User> getAllUsers();

    List<User> findByUsername(String username);

    boolean existsByUsernameIgnoreCase(String username);

    List<User> findByEmailIgnoreCase(String email);

    /**
     * Gets users filtered by username and email with pagination, excluding admins
     * 
     * @param username Optional filter by username
     * @param email    Optional filter by email
     * @param pageable Pagination information
     * @return Page of filtered users
     */
    Page<User> getFilteredUsersPaginated(String username, String email, Pageable pageable);

}
