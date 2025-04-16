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
     * Create a new review
     * 
     * @param authHeader Authorization token
     * @param reviewDTO Review data to create
     * @return Created review with 201 status code
     */
    @PostMapping
    public ResponseEntity<?> createReview(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ReviewDTO reviewDTO) {
        
        User user = jwtService.getUser(authHeader);
        
        Route route = routeService.findById(reviewDTO.getRouteId());
        if (route == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<Review> existingReviews = reviewService.findByUserAndRoute(user, route);
        if (!existingReviews.isEmpty()) {
            return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body("You already have a review for this route. Please update your existing review instead.");
        }
        
        reviewDTO.setId(null);
        
        Review review = reviewDTO.toEntity(user, route);
        
        review = reviewService.saveReview(review, user);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(new ReviewDTO(review));
    }
    
    /**
     * Update an existing review
     * 
     * @param authHeader Authorization token
     * @param reviewDTO Updated review data
     * @return Updated review or appropriate error status
     */
    @PutMapping
    public ResponseEntity<?> updateReview(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ReviewDTO reviewDTO) {
        
        User user = jwtService.getUser(authHeader);
        
        if (reviewDTO.getId() == null) {
            return ResponseEntity.badRequest().body("Review ID is required for update");
        }
        
        Review existingReview = reviewService.findById(reviewDTO.getId());
        if (existingReview == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (!reviewService.isReviewOwner(reviewDTO.getId(), user.getId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Route route = existingReview.getRoute();
        
        Review review = reviewDTO.toEntity(user, route);
        
        review = reviewService.saveReview(review, user);
        
        return ResponseEntity.ok(new ReviewDTO(review));
    }
    
    /**
     * Delete a review
     * 
     * @param authHeader Authorization token
     * @param reviewId ID of the review to delete
     * @return Empty response with appropriate status
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long reviewId) {
        
        User user = jwtService.getUser(authHeader);
        
        Review review = reviewService.findById(reviewId);
        if (review == null) {
            return ResponseEntity.notFound().build();
        }
        
        if (!reviewService.isReviewOwner(reviewId, user.getId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        boolean deleted = reviewService.deleteReview(reviewId);
        
        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
    
    /**
     * Get a review by its ID
     * 
     * @param authHeader Authorization token
     * @param reviewId ID of the review to retrieve
     * @return The requested review or 404 if it doesn't exist
     */
    @GetMapping("/{reviewId}")
    public ResponseEntity<?> getReview(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long reviewId) {
        
        Review review = reviewService.findById(reviewId);
        if (review == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(new ReviewDTO(review));
    }
    
    /**
     * Get all reviews for a route with pagination
     * 
     * @param authHeader Authorization token
     * @param routeId ID of the route
     * @param lastReviewId ID of the last review received (optional, for pagination)
     * @return List of reviews
     */
    @GetMapping("/route/{routeId}")
    public ResponseEntity<?> getReviewsByRoute(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long routeId,
            @RequestParam(required = false) Long lastReviewId) {
        
        Route route = routeService.findById(routeId);
        if (route == null) {
            return ResponseEntity.notFound().build();
        }
        
        List<Review> reviews = reviewService.findReviewsByRouteId(routeId, lastReviewId);
        
        List<ReviewDTO> reviewDTOs = reviews.stream()
                .map(ReviewDTO::new)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(reviewDTOs);
    }
    
    /**
     * Get the current user's review for a specific route
     * 
     * @param authHeader Authorization token
     * @param routeId ID of the route
     * @return The user's review or 404 if not found
     */
    @GetMapping("/my/route/{routeId}")
    public ResponseEntity<?> getMyReviewForRoute(
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
}