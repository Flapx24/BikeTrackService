package com.example.demo.servicesImpl;

import java.text.Normalizer;
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

import com.example.demo.dtos.RouteDTO;
import com.example.demo.entities.Route;
import com.example.demo.enums.RouteDetailLevel;
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
    public List<RouteDTO> getFilteredRoutes(String city, String title, String sort) {
        List<Route> routes = new ArrayList<>();

        switch (sort) {
            case "asc":
                routes = routeRepository.findByCityContainingAndTitleContainingOrderByAverageReviewScoreDesc(city,
                        title);
                break;
            case "desc":
                routes = routeRepository.findByCityContainingAndTitleContainingOrderByAverageReviewScoreAsc(city,
                        title);
                break;
            default:
                routes = routeRepository.findByCityContainingAndTitleContainingIgnoreCase(city, title);
                break;
        }

        return routes.stream()
                .map(route -> {
                    RouteDTO dto = RouteDTO.fromEntity(route, RouteDetailLevel.BASIC);
                    dto.setReviewCount(route.getReviews() != null ? route.getReviews().size() : 0);
                    dto.setUpdateCount(route.getUpdates() != null ? route.getUpdates().size() : 0);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Page<RouteDTO> getFilteredRoutesPaginated(String city, String title, String sort, Pageable pageable) {
        Page<Route> routes;

        switch (sort) {
            case "asc":
                routes = routeRepository.findByCityContainingAndTitleContainingOrderByAverageReviewScoreDescPaginated(
                        city, title, pageable);
                break;
            case "desc":
                routes = routeRepository.findByCityContainingAndTitleContainingOrderByAverageReviewScoreAscPaginated(
                        city, title, pageable);
                break;
            default:
                routes = routeRepository.findByCityContainingAndTitleContainingIgnoreCasePaginated(city, title,
                        pageable);
                break;
        }

        return routes.map(route -> {
            RouteDTO dto = RouteDTO.fromEntity(route, RouteDetailLevel.BASIC);
            dto.setReviewCount(route.getReviews() != null ? route.getReviews().size() : 0);
            dto.setUpdateCount(route.getUpdates() != null ? route.getUpdates().size() : 0);
            return dto;
        });
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

        String normalized = Normalizer.normalize(city, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}", "")
                .toLowerCase();

        return normalized;
    }
}