package com.example.demo.services;

import java.util.List;

import com.example.demo.entities.Workshop;

public interface WorkshopService {
    
    Workshop findById(Long id);
    
    List<Workshop> findByCity(String city);
    
    Workshop saveWorkshop(Workshop workshop);
    
    /**
     * Normalizes a city name for searching
     * Converts to lowercase and removes accents
     * 
     * @param city City name to normalize
     * @return Normalized city name
     */
    String normalizeCity(String city);
}