package com.example.demo.servicesImpl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dtos.ReviewDTO;
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
    public List<Review> findReviewsByRouteId(Long routeId, Long lastReviewId, User requestingUser) {
        Route route = routeRepository.findById(routeId).orElse(null);

        if (route == null) {
            return List.of();
        }

        List<Review> result = new ArrayList<>();
        Pageable pageable = PageRequest.of(0, PAGE_SIZE);

        // First page: check if user has a review and include it first
        if (lastReviewId == null && requestingUser != null) {
            List<Review> userReviews = reviewRepository.findByUserAndRoute(requestingUser, route);
            if (!userReviews.isEmpty()) {
                result.add(userReviews.get(0)); // Add user's review first

                // Get remaining reviews excluding user's review
                List<Review> otherReviews = reviewRepository.findByRouteExcludingUserOrderByIdDesc(
                        route, requestingUser, PageRequest.of(0, PAGE_SIZE - 1));
                result.addAll(otherReviews);
            } else {
                // No user review, get all reviews in descending order
                result = reviewRepository.findByRouteOrderByIdDesc(route, pageable);
            }
        } else if (lastReviewId == null) {
            // No user specified, get all reviews in descending order
            result = reviewRepository.findByRouteOrderByIdDesc(route, pageable);
        } else {
            // Pagination: exclude user's review from subsequent pages
            if (requestingUser != null) {
                result = reviewRepository.findByRouteExcludingUserAndIdLessThanOrderByIdDesc(
                        route, requestingUser, lastReviewId, pageable);
            } else {
                result = reviewRepository.findByRouteAndIdLessThanOrderByIdDesc(
                        route, lastReviewId, pageable);
            }
        }

        return result;
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

        if (averageScore == null) {
            averageScore = 0.0;
        }

        route.setAverageReviewScore(averageScore);
        routeRepository.save(route);
    }

    @Override
    public List<Review> findByUserAndRoute(User user, Route route) {
        return reviewRepository.findByUserAndRoute(user, route);
    }

    @Override
    public List<Review> findAllReviews() {
        return reviewRepository.findAll();
    }

    @Override
    public boolean filterByCity(Review review, String city) {
        if (city == null || city.trim().isEmpty()) {
            return true;
        }

        Route route = review.getRoute();
        if (route == null || route.getCity() == null) {
            return false;
        }

        String normalizedRouteCity = normalizeString(route.getCity());
        String normalizedFilterCity = normalizeString(city);

        return normalizedRouteCity.contains(normalizedFilterCity);
    }

    @Override
    public boolean filterByDate(Review review, String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return true;
        }

        try {
            LocalDate filterDate = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            LocalDate reviewDate = review.getDate();

            return reviewDate != null && reviewDate.equals(filterDate);
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    @Override
    public String normalizeString(String input) {
        if (input == null) {
            return "";
        }

        return input.toLowerCase()
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u")
                .replace("ü", "u")
                .replace("ñ", "n");
    }

    @Override
    public ReviewDTO createReviewDTOWithRouteTitle(Review review) {
        ReviewDTO dto = new ReviewDTO(review);
        if (review.getRoute() != null) {
            dto.setRouteTitle(review.getRoute().getTitle());
            dto.setRouteCity(review.getRoute().getCity());
        }
        return dto;
    }

    @Override
    public List<ReviewDTO> getFilteredReviews(String routeName, String city, String date) {
        List<Review> allReviews = findAllReviews();
        return allReviews.stream()
                .filter(review -> filterByRouteName(review, routeName))
                .filter(review -> filterByCity(review, city))
                .filter(review -> filterByDate(review, date))
                .map(this::createReviewDTOWithRouteTitle)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ReviewDTO> getFilteredReviewsPaginated(String routeName, String city, String date, Pageable pageable) {

        Page<Review> reviewPage = reviewRepository.findByRouteNameContainingAndCityContainingAndDatePaginated(routeName,
                city, date, pageable);

        return reviewPage.map(this::createReviewDTOWithRouteTitle);
    }

    @Override
    public boolean filterByRouteName(Review review, String routeName) {
        if (routeName == null || routeName.trim().isEmpty()) {
            return true;
        }

        if (review.getRoute() == null || review.getRoute().getTitle() == null) {
            return false;
        }

        String normalizedRouteTitle = normalizeString(review.getRoute().getTitle());
        String normalizedFilterRouteName = normalizeString(routeName.trim());

        return normalizedRouteTitle.contains(normalizedFilterRouteName);
    }
}