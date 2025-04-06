package com.example.demo.services;

import java.util.Date;

public interface BicycleMaintenanceService {
    
    /**
     * Reset a specific component after maintenance
     * Also updates the bicycle's maintenance date
     * 
     * @param bicycleId The bicycle ID
     * @param componentId The component ID
     * @return true if successful, false otherwise
     */
    boolean resetComponent(Long bicycleId, Long componentId);
    
    /**
     * Reset a specific component by name
     * Also updates the bicycle's maintenance date
     * 
     * @param bicycleId The bicycle ID
     * @param componentName The component name
     * @return true if successful, false otherwise
     */
    boolean resetComponentByName(Long bicycleId, String componentName);
    
    /**
     * Perform maintenance on a component without resetting kilometers
     * 
     * @param bicycleId The bicycle ID
     * @param componentId The component ID
     * @return true if successful, false otherwise
     */
    boolean performMaintenanceOnComponent(Long bicycleId, Long componentId);
    
    /**
     * Perform maintenance on a component by name
     * 
     * @param bicycleId The bicycle ID
     * @param componentName The component name
     * @return true if successful, false otherwise
     */
    boolean performMaintenanceOnComponentByName(Long bicycleId, String componentName);
    
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
     * Perform full maintenance on entire bicycle
     * Updates the last maintenance date
     * 
     * @param bicycleId The bicycle ID
     * @return true if successful, false otherwise
     */
    boolean performFullMaintenance(Long bicycleId);
    
    /**
     * Get the last maintenance date for a bicycle
     * 
     * @param bicycleId The bicycle ID
     * @return The last maintenance date or null if not found
     */
    Date getLastMaintenanceDate(Long bicycleId);

}
