package com.example.demo.repositories;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entities.Bicycle;
import com.example.demo.entities.User;

@Repository("bicycleRepository")
public interface BicycleRepository extends JpaRepository<Bicycle, Serializable> {
    
    List<Bicycle> findByOwner(User owner);
    
    List<Bicycle> findByOwnerAndNameContainingIgnoreCase(User owner, String name);
    
    boolean existsByNameAndOwner(String name, User owner);
}
