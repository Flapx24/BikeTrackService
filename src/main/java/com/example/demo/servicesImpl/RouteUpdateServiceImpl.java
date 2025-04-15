package com.example.demo.servicesImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entities.RouteUpdate;
import com.example.demo.repositories.RouteUpdateRepository;
import com.example.demo.services.RouteUpdateService;

@Service("routeUpdateService")
public class RouteUpdateServiceImpl implements RouteUpdateService {

    @Autowired
    @Qualifier("routeUpdateRepository")
    private RouteUpdateRepository routeUpdateRepository;
    
    @Override
    @Transactional
    public RouteUpdate saveRouteUpdate(RouteUpdate routeUpdate) {
        return routeUpdateRepository.save(routeUpdate);
    }

    @Override
    public RouteUpdate findById(Long id) {
        return routeUpdateRepository.findById(id).orElse(null);
    }

    @Override
    public List<RouteUpdate> findByRouteId(Long routeId) {
        return routeUpdateRepository.findByRouteId(routeId);
    }

    @Override
    @Transactional
    public boolean deleteRouteUpdate(Long id) {
        if (routeUpdateRepository.existsById(id)) {
            routeUpdateRepository.deleteById(id);
            return true;
        }
        return false;
    }
}