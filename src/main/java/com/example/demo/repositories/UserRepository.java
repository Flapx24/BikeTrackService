package com.example.demo.repositories;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entities.User;

@Repository("userRepository")
public interface UserRepository extends JpaRepository<User, Serializable> {

    boolean existsByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);

    User findByEmail(String email);

    List<User> findAll();

    List<User> findByEmailIgnoreCase(String email);

    List<User> findByUsernameIgnoreCase(String username);

    List<User> findByUsernameContainingIgnoreCase(String username);

}
