package com.example.demo.servicesImpl;

import java.text.Normalizer;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entities.Route;
import com.example.demo.repositories.RouteRepository;
import com.example.demo.services.RouteService;

@Service("routeService")
public class RouteServiceImpl implements RouteService {

    private static final int PAGE_SIZE = 10;

    @Autowired
    @Qualifier("routeRepository")
    private RouteRepository routeRepository;

    @Override
    @Transactional
    public Route saveRoute(Route route) {
        if (route.getCity() != null) {
            route.setCity(normalizeCity(route.getCity()));
        }
        return routeRepository.save(route);
    }

    @Override
    public Route findById(Long id) {
        return routeRepository.findById(id).orElse(null);
    }

    @Override
    public List<Route> getAllRoutes(Long lastRouteId) {
        if (lastRouteId == null) {
            return routeRepository.findAllByOrderByIdAsc(PageRequest.of(0, PAGE_SIZE));
        } else {
            return routeRepository.findAllWithIdGreaterThan(lastRouteId, PageRequest.of(0, PAGE_SIZE));
        }
    }

    @Override
    public List<Route> getRoutesByCityAndMinScore(String city, Integer minScore, Long lastRouteId) {
        double minScoreDouble = minScore != null ? minScore.doubleValue() : 0.0;
        
        String normalizedCity = normalizeCity(city);
        
        if (lastRouteId == null) {
            return routeRepository.findByCityAndMinScore(
                normalizedCity, minScoreDouble, PageRequest.of(0, PAGE_SIZE));
        } else {
            return routeRepository.findByCityAndMinScoreAndIdGreaterThan(
                normalizedCity, minScoreDouble, lastRouteId, PageRequest.of(0, PAGE_SIZE));
        }
    }

    @Override
    @Transactional
    public boolean deleteRoute(Long id) {
        if (routeRepository.existsById(id)) {
            routeRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public String normalizeCity(String city) {
        if (city == null) {
            return "";
        }
        
        String normalized = city.toLowerCase();
        
        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}", "");
        
        return normalized;
    }
}