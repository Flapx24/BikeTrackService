package com.example.demo.repositories;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entities.Workshop;

public interface WorkshopRepository extends JpaRepository<Workshop, Serializable>{

    /**
     * Find all workshops in a specific city (case insensitive)
     * 
     * @param city Name of the city
     * @return List of workshops in the city
     */
    @Query("SELECT w FROM Workshop w WHERE LOWER(w.city) = LOWER(:city)")
    List<Workshop> findByCity(@Param("city") String city);
}
