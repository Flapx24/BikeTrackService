package com.example.demo.repositories;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entities.Route;

@Repository("routeRepository")
public interface RouteRepository extends JpaRepository<Route, Serializable>{
    
    @Query("SELECT r FROM Route r WHERE r.id > :lastId ORDER BY r.id ASC")
    List<Route> findAllWithIdGreaterThan(@Param("lastId") Long lastId, Pageable pageable);
    
    List<Route> findAllByOrderByIdAsc(Pageable pageable);
    
    @Query("SELECT r FROM Route r WHERE LOWER(r.city) = LOWER(:city) AND r.averageReviewScore >= :minScore AND r.id > :lastId ORDER BY r.id ASC")
    List<Route> findByCityAndMinScoreAndIdGreaterThan(
            @Param("city") String city, 
            @Param("minScore") Double minScore, 
            @Param("lastId") Long lastId,
            Pageable pageable);
    
    @Query("SELECT r FROM Route r WHERE LOWER(r.city) = LOWER(:city) AND r.averageReviewScore >= :minScore ORDER BY r.id ASC")
    List<Route> findByCityAndMinScore(
            @Param("city") String city, 
            @Param("minScore") Double minScore,
            Pageable pageable);
}
