package com.example.demo.services;

import java.util.List;

import com.example.demo.entities.RouteUpdate;

public interface RouteUpdateService {
    
    RouteUpdate saveRouteUpdate(RouteUpdate routeUpdate);
    
    RouteUpdate findById(Long id);
    
    List<RouteUpdate> findByRouteId(Long routeId);
    
    boolean deleteRouteUpdate(Long id);
}