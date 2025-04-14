package com.example.demo.repositories;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.demo.entities.Review;
import com.example.demo.entities.Route;
import com.example.demo.entities.User;

@Repository("reviewRepository")
public interface ReviewRepository extends JpaRepository<Review, Serializable> {
    
    List<Review> findByRoute(Route route);

    List<Review> findByRouteOrderByIdAsc(Route route, Pageable pageable);
    
    @Query("SELECT r FROM Review r WHERE r.route = :route AND r.id > :lastId ORDER BY r.id ASC")
    List<Review> findByRouteAndIdGreaterThan(
            @Param("route") Route route, 
            @Param("lastId") Long lastId,
            Pageable pageable);
    
    List<Review> findByUserAndRoute(User user, Route route);
    
    /**
     * Calculate the average rating for a route
     * 
     * @param route Route to calculate average for
     * @return Average rating or 0 if no reviews
     */
    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.route = :route")
    Double calculateAverageRatingForRoute(@Param("route") Route route);
}
