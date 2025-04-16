package com.example.demo.services;

import java.util.List;

import com.example.demo.entities.Review;
import com.example.demo.entities.User;
import com.example.demo.entities.Route;

public interface ReviewService {
    
    Review saveReview(Review review, User user);
    
    Review findById(Long id);
    
    List<Review> findReviewsByRouteId(Long routeId, Long lastReviewId);
    
    boolean deleteReview(Long id);
    
    boolean isReviewOwner(Long reviewId, Long userId);
    
    void updateRouteAverageScore(Long routeId);
    
    List<Review> findByUserAndRoute(User user, Route route);
}