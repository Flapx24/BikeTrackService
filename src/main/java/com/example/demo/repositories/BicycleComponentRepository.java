package com.example.demo.repositories;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entities.Bicycle;
import com.example.demo.entities.BicycleComponent;

@Repository("bicycleComponentRepository")
public interface BicycleComponentRepository extends JpaRepository<BicycleComponent, Serializable> {
    
    List<BicycleComponent> findByBicycle(Bicycle bicycle);
    
    List<BicycleComponent> findByBicycleAndName(Bicycle bicycle, String name);
    
    @Query("SELECT c FROM BicycleComponent c WHERE c.bicycle = :bicycle AND c.currentKilometers >= c.maxKilometers")
    List<BicycleComponent> findComponentsNeedingMaintenance(@Param("bicycle") Bicycle bicycle);
    
    void deleteByBicycle(Bicycle bicycle);
}
