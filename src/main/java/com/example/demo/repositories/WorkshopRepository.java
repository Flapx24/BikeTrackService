package com.example.demo.repositories;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entities.Workshop;

@Repository("workshopRepository")
public interface WorkshopRepository extends JpaRepository<Workshop, Serializable> {

       /**
        * Find all workshops in a specific city (case insensitive)
        * 
        * @param city Name of the city
        * @return List of workshops in the city
        */
       @Query("SELECT w FROM Workshop w WHERE LOWER(w.city) = LOWER(:city)")
       List<Workshop> findByCity(@Param("city") String city);

       @Query("SELECT w FROM Workshop w WHERE " +
                     "(:city IS NULL OR :city = '' OR LOWER(w.city) LIKE LOWER(CONCAT('%', :city, '%'))) AND " +
                     "(:name IS NULL OR :name = '' OR LOWER(w.name) LIKE LOWER(CONCAT('%', :name, '%')))")
       Page<Workshop> findByCityContainingAndNameContainingIgnoreCasePaginated(
                     @Param("city") String city,
                     @Param("name") String name,
                     Pageable pageable);
}
