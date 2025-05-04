package com.example.demo.services;

import java.util.List;

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
}