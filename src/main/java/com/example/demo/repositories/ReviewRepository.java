package com.example.demo.repositories;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.domain.Page;
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

        List<Review> findByUserAndRoute(User user, Route route);

        // Methods for descending order with user review exclusion
        @Query("SELECT r FROM Review r WHERE r.route = :route ORDER BY r.id DESC")
        List<Review> findByRouteOrderByIdDesc(@Param("route") Route route, Pageable pageable);

        @Query("SELECT r FROM Review r WHERE r.route = :route AND r.id < :lastId ORDER BY r.id DESC")
        List<Review> findByRouteAndIdLessThanOrderByIdDesc(
                        @Param("route") Route route,
                        @Param("lastId") Long lastId,
                        Pageable pageable);

        @Query("SELECT r FROM Review r WHERE r.route = :route AND r.user != :user ORDER BY r.id DESC")
        List<Review> findByRouteExcludingUserOrderByIdDesc(
                        @Param("route") Route route,
                        @Param("user") User user,
                        Pageable pageable);

        @Query("SELECT r FROM Review r WHERE r.route = :route AND r.user != :user AND r.id < :lastId ORDER BY r.id DESC")
        List<Review> findByRouteExcludingUserAndIdLessThanOrderByIdDesc(
                        @Param("route") Route route,
                        @Param("user") User user,
                        @Param("lastId") Long lastId,
                        Pageable pageable);

        /**
         * Calculate the average rating for a route
         * 
         * @param route Route to calculate average for
         * @return Average rating or 0 if no reviews
         */
        @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.route = :route")
        Double calculateAverageRatingForRoute(@Param("route") Route route);

        List<Review> findByUser(User user);

        @Query("SELECT r FROM Review r WHERE r.route.id = :routeId ORDER BY r.id DESC")
        List<Review> findAllByRouteIdOrderByIdDesc(@Param("routeId") Long routeId);

        @Query("SELECT r FROM Review r WHERE r.route.id = :routeId AND r.id < :lastReviewId ORDER BY r.id DESC")
        List<Review> findAllByRouteIdAndIdLessThanOrderByIdDesc(
                        @Param("routeId") Long routeId,
                        @Param("lastReviewId") Long lastReviewId);

        @Query("SELECT r FROM Review r WHERE " +
                        "(:city IS NULL OR :city = '' OR LOWER(r.route.city) LIKE LOWER(CONCAT('%', :city, '%'))) AND "
                        +
                        "(:date IS NULL OR :date = '' OR FUNCTION('DATE_FORMAT', r.date, '%d/%m/%Y') = :date)")
        Page<Review> findByCityContainingAndDatePaginated(
                        @Param("city") String city,
                        @Param("date") String date,
                        Pageable pageable);

        @Query("SELECT r FROM Review r WHERE " +
                        "(:routeName IS NULL OR :routeName = '' OR LOWER(r.route.title) LIKE LOWER(CONCAT('%', :routeName, '%'))) AND "
                        +
                        "(:city IS NULL OR :city = '' OR LOWER(r.route.city) LIKE LOWER(CONCAT('%', :city, '%'))) AND "
                        +
                        "(:date IS NULL OR :date = '' OR FUNCTION('DATE_FORMAT', r.date, '%d/%m/%Y') = :date)")
        Page<Review> findByRouteNameContainingAndCityContainingAndDatePaginated(
                        @Param("routeName") String routeName,
                        @Param("city") String city,
                        @Param("date") String date,
                        Pageable pageable);
}
