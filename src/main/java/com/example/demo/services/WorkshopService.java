package com.example.demo.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.demo.entities.Workshop;

public interface WorkshopService {

    Workshop findById(Long id);

    List<Workshop> findByCity(String city);

    Workshop saveWorkshop(Workshop workshop);

    List<Workshop> findAll();

    boolean deleteWorkshop(Long id);

    /**
     * Normalizes a city name for searching
     * Converts to lowercase and removes accents
     * 
     * @param city City name to normalize
     * @return Normalized city name
     */
    String normalizeCity(String city);

    /**
     * Normalizes any string for searching/filtering
     * Converts to lowercase and removes accents
     * 
     * @param text Text to normalize
     * @return Normalized text
     */
    String normalizeString(String text);

    /**
     * Gets workshops filtered by city and name with pagination
     * 
     * @param city     Optional filter by city
     * @param name     Optional filter by name
     * @param pageable Pagination information
     * @return Page of filtered workshops
     */
    Page<Workshop> getFilteredWorkshopsPaginated(String city, String name, Pageable pageable);
}