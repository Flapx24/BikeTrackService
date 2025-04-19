package com.example.demo.controllers.api;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dtos.ReviewDTO;
import com.example.demo.entities.Review;
import com.example.demo.entities.Route;
import com.example.demo.entities.User;
import com.example.demo.services.ReviewService;
import com.example.demo.services.RouteService;
import com.example.demo.servicesImpl.JwtService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
    
    @Autowired
    @Qualifier("reviewService")
    private ReviewService reviewService;
    
    @Autowired
    @Qualifier("routeService")
    private RouteService routeService;
    
    @Autowired
    @Qualifier("jwtService")
    private JwtService jwtService;
    
    /**
     * Create a review for a route
     * 
     * @param authHeader Authorization token
     * @param routeId ID of the route to review
     * @param reviewDTO Review data to create
     * @return Created review with 201 status code
     */
    @PostMapping("/route/{routeId}")
    public ResponseEntity<?> createReview(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long routeId,
            @Valid @RequestBody ReviewDTO reviewDTO) {
        
        User user = jwtService.getUser(authHeader);
        
        Route route = routeService.findById(routeId);
        if (route == null) {
            return ResponseEntity.notFound().build();
        }
        
        reviewDTO.setRouteId(routeId);
        
        List<Review> existingReviews = reviewService.findByUserAndRoute(user, route);
        if (!existingReviews.isEmpty()) {
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body("Ya tienes una reseña para esta ruta. Por favor, actualiza tu reseña existente.");
        }
        
        reviewDTO.setId(null);
        
        Review review = reviewDTO.toEntity(user, route);
        
        review = reviewService.saveReview(review, user);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(new ReviewDTO(review));
    }
    
    /**
     * Update the current user's review for a specific route
     * 
     * @param authHeader Authorization token
     * @param routeId ID of the route
     * @param reviewDTO Updated review data (rating and text)
     * @return Updated review or 404 if the user hasn't reviewed this route
     */
    @PutMapping("/route/{routeId}")
    public ResponseEntity<?> updateReview(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long routeId,
            @Valid @RequestBody ReviewDTO reviewDTO) {
        
        User user = jwtService.getUser(authHeader);
        
        Route route = routeService.findById(routeId);
        if (route == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<Review> existingReviews = reviewService.findByUserAndRoute(user, route);
        if (existingReviews.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Review existingReview = existingReviews.get(0);
        
        reviewDTO.setId(existingReview.getId());
        reviewDTO.setRouteId(routeId);
        
        Review review = reviewDTO.toEntity(user, route);
        
        review = reviewService.saveReview(review, user);
        
        return ResponseEntity.ok(new ReviewDTO(review));
    }
    
    /**
     * Delete the current user's review for a specific route
     * 
     * @param authHeader Authorization token
     * @param routeId ID of the route
     * @return Empty response with appropriate status (204 if deleted, 404 if not found)
     */
    @DeleteMapping("/route/{routeId}")
    public ResponseEntity<?> deleteReview(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long routeId) {
        
        User user = jwtService.getUser(authHeader);
        
        Route route = routeService.findById(routeId);
        if (route == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<Review> existingReviews = reviewService.findByUserAndRoute(user, route);
        if (existingReviews.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Review review = existingReviews.get(0);
        
        boolean deleted = reviewService.deleteReview(review.getId());
        
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
    
    /**
     * Get the current user's review for a specific route
     * 
     * @param authHeader Authorization token
     * @param routeId ID of the route
     * @return The user's review or 404 if not found
     */
    @GetMapping("/route/{routeId}/mine")
    public ResponseEntity<?> getMyReview(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long routeId) {
        
        User user = jwtService.getUser(authHeader);
        
        Route route = routeService.findById(routeId);
        if (route == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<Review> existingReviews = reviewService.findByUserAndRoute(user, route);
        if (existingReviews.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Review review = existingReviews.get(0);
        
        return ResponseEntity.ok(new ReviewDTO(review));
    }
    
    /**
     * Get all reviews for a route with pagination
     * 
     * @param authHeader Authorization token
     * @param routeId ID of the route
     * @param lastReviewId ID of the last review received (optional, for pagination)
     * @return List of reviews (empty list if no reviews exist)
     */
    @GetMapping("/route/{routeId}")
    public ResponseEntity<?> getRouteReviews(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long routeId,
            @RequestParam(required = false) Long lastReviewId) {
        
        try {
            Route route = routeService.findById(routeId);
            if (route == null) {
                return ResponseEntity.notFound().build();
            }
            
            List<Review> reviews = reviewService.findReviewsByRouteId(routeId, lastReviewId);
            
            // Si no hay reseñas, devuelve una lista vacía (200 OK) en lugar de 404
            List<ReviewDTO> reviewDTOs = (reviews == null || reviews.isEmpty()) 
                ? List.of() 
                : reviews.stream()
                    .map(ReviewDTO::new)
                    .collect(Collectors.toList());
            
            return ResponseEntity.ok(reviewDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al obtener las reseñas: " + e.getMessage());
        }
    }
}