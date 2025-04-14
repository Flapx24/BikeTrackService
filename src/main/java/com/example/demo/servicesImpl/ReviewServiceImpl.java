package com.example.demo.servicesImpl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entities.Review;
import com.example.demo.entities.Route;
import com.example.demo.entities.User;
import com.example.demo.repositories.ReviewRepository;
import com.example.demo.repositories.RouteRepository;
import com.example.demo.services.ReviewService;

@Service("reviewService")
public class ReviewServiceImpl implements ReviewService {

    private static final int PAGE_SIZE = 15;

    @Autowired
    @Qualifier("reviewRepository")
    private ReviewRepository reviewRepository;
    
    @Autowired
    @Qualifier("routeRepository")
    private RouteRepository routeRepository;
    
    @Override
    @Transactional
    public Review saveReview(Review review, User user) {
        if (review.getDate() == null) {
            review.setDate(LocalDate.now());
        }
        
        review.setUser(user);
        
        Review savedReview = reviewRepository.save(review);
        
        updateRouteAverageScore(savedReview.getRoute().getId());
        
        return savedReview;
    }

    @Override
    public Review findById(Long id) {
        return reviewRepository.findById(id).orElse(null);
    }

    @Override
    public List<Review> findReviewsByRouteId(Long routeId, Long lastReviewId) {
        Route route = routeRepository.findById(routeId).orElse(null);
        
        if (route == null) {
            return List.of();
        }
        
        if (lastReviewId == null) {
            return reviewRepository.findByRouteOrderByIdAsc(route, PageRequest.of(0, PAGE_SIZE));
        } else {
            return reviewRepository.findByRouteAndIdGreaterThan(route, lastReviewId, PageRequest.of(0, PAGE_SIZE));
        }
    }

    @Override
    @Transactional
    public boolean deleteReview(Long id) {
        Review review = findById(id);
        
        if (review == null) {
            return false;
        }
        
        Long routeId = review.getRoute().getId();
        
        reviewRepository.deleteById(id);
        
        updateRouteAverageScore(routeId);
        
        return true;
    }

    @Override
    public boolean isReviewOwner(Long reviewId, Long userId) {
        Review review = findById(reviewId);
        
        if (review == null || review.getUser() == null) {
            return false;
        }
        
        return review.getUser().getId().equals(userId);
    }

    @Override
    @Transactional
    public void updateRouteAverageScore(Long routeId) {
        Route route = routeRepository.findById(routeId).orElse(null);
        
        if (route == null) {
            return;
        }
        
        Double averageScore = reviewRepository.calculateAverageRatingForRoute(route);
        
        route.setAverageReviewScore(averageScore);
        routeRepository.save(route);
    }
}