package com.example.demo.repositories;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entities.RouteUpdate;

@Repository("routeUpdateRepository")
public interface RouteUpdateRepository extends JpaRepository<RouteUpdate, Serializable> {
    
    List<RouteUpdate> findByRouteId(Long routeId);
}
