package com.example.demo.services;

import java.util.Date;

public interface BicycleMaintenanceService {
    
    /**
     * Reset a specific component's kilometers to zero
     * Does NOT update the bicycle's maintenance date
     * 
     * @param bicycleId The bicycle ID
     * @param componentId The component ID
     * @return true if successful, false otherwise
     */
    boolean resetComponent(Long bicycleId, Long componentId);
    
    /**
     * Update the maximum kilometers for a component
     * Also updates the bicycle's maintenance date (considered a part replacement)
     * 
     * @param bicycleId The bicycle ID
     * @param componentId The component ID
     * @param maxKilometers The new maximum kilometers
     * @return true if successful, false otherwise
     */
    boolean updateComponentMaxKilometers(Long bicycleId, Long componentId, Double maxKilometers);
    
    /**
     * Register maintenance for the bicycle with current date
     * Only updates the maintenance date without modifying any components
     * 
     * @param bicycleId The bicycle ID
     * @return true if successful, false otherwise
     */
    boolean registerMaintenance(Long bicycleId);
    
    /**
     * Register maintenance for the bicycle with a specific date
     * Only updates the maintenance date without modifying any components
     * 
     * @param bicycleId The bicycle ID
     * @param maintenanceDate The specific maintenance date to record
     * @return true if successful, false otherwise
     */
    boolean registerMaintenanceWithDate(Long bicycleId, Date maintenanceDate);
    
    /**
     * Get the last maintenance date for a bicycle
     * 
     * @param bicycleId The bicycle ID
     * @return The last maintenance date or null if not found
     */
    Date getLastMaintenanceDate(Long bicycleId);

}
