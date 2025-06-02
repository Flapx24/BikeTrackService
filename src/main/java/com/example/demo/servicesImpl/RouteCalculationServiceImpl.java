package com.example.demo.servicesImpl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.demo.dtos.CalculatedRouteDTO;
import com.example.demo.enums.VehicleType;
import com.example.demo.models.GeoPoint;
import com.example.demo.services.RouteCalculationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import reactor.core.publisher.Mono;

@Service("routeCalculationService")
public class RouteCalculationServiceImpl implements RouteCalculationService {
    private static final Logger logger = LoggerFactory.getLogger(RouteCalculationServiceImpl.class);
    private static final int MAX_POINTS = 50;
    private static final int MIN_POINTS = 2;
    private final WebClient webClient;
    private final String apiKey;
    private final ObjectMapper objectMapper;

    public RouteCalculationServiceImpl(
            @Value("${openrouteservice.api.url:https://api.openrouteservice.org/v2}") String apiUrl,
            @Value("${openrouteservice.api.key:}") String apiKey) {
        this.apiKey = apiKey;

        logger.info("Initializing RouteCalculationServiceImpl with API URL: {} and key available: {}",
                apiUrl, (apiKey != null && !apiKey.isEmpty() ? "yes" : "no"));

        this.webClient = WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public CalculatedRouteDTO calculateRoute(List<GeoPoint> points, VehicleType vehicleType) {
        if (points == null || points.size() < MIN_POINTS) {
            return new CalculatedRouteDTO("At least 2 points are required to calculate a route");
        }

        if (points.size() > MAX_POINTS) {
            return new CalculatedRouteDTO("Cannot calculate routes with more than 50 points");
        }

        if (apiKey == null || apiKey.isEmpty()) {
            logger.error("API key is missing for OpenRouteService");
            return new CalculatedRouteDTO("API key configuration is missing");
        }

        try {
            logger.info("Calculating route for {} points with vehicle type: {}", points.size(), vehicleType.name());

            String profile = vehicleType.getProfile();

            // Call the OpenRouteService API with the specific geojson endpoint
            String responseJson = null;
            try {
                responseJson = callOpenRouteServiceDirections(points, profile).block();
            } catch (Exception e) {
                // Check if it's the specific error about the endpoint not being found
                if (e.getMessage() != null && e.getMessage().contains("intentando alternativa")) {
                    logger.info("Trying standard endpoint without geojson suffix");

                    // Create an alternative WebClient with standard endpoint
                    WebClient standardWebClient = WebClient.builder()
                            .baseUrl("https://api.openrouteservice.org/v2")
                            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                            .build();

                    ObjectNode requestBody = objectMapper.createObjectNode();
                    ArrayNode coordinates = objectMapper.createArrayNode();
                    for (GeoPoint point : points) {
                        ArrayNode coordinate = objectMapper.createArrayNode();
                        coordinate.add(point.getLng());
                        coordinate.add(point.getLat());
                        coordinates.add(coordinate);
                    }
                    requestBody.set("coordinates", coordinates);
                    requestBody.put("format", "geojson");

                    // Try with standard endpoint
                    responseJson = standardWebClient.post()
                            .uri("/directions/" + profile)
                            .header(HttpHeaders.AUTHORIZATION,
                                    apiKey.startsWith("Bearer ") ? apiKey : "Bearer " + apiKey)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(requestBody.toString())
                            .retrieve()
                            .bodyToMono(String.class)
                            .block();
                } else {
                    throw e;
                }
            }

            if (responseJson == null) {
                return new CalculatedRouteDTO("No response from route service");
            }

            return processDirectionsResponse(responseJson, vehicleType);
        } catch (Exception e) {
            logger.error("Error calculating route", e);
            String errorMsg = e.getMessage();

            if (errorMsg != null) {
                if (errorMsg.contains("403")) {
                    return new CalculatedRouteDTO("Authentication error - please verify your API key");
                } else if (errorMsg.contains("429")) {
                    return new CalculatedRouteDTO(
                            "Too many requests - API rate limit exceeded. Please try again later");
                } else if (errorMsg.contains("format=geojson")) {
                    return new CalculatedRouteDTO("Error with GeoJSON format. Trying different format...");
                }
            }

            return new CalculatedRouteDTO("Error calculating route: " + errorMsg);
        }
    }

    private Mono<String> callOpenRouteServiceDirections(List<GeoPoint> points, String profile) {
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();

            // Add coordinates (longitude, latitude format for OpenRouteService)
            ArrayNode coordinates = objectMapper.createArrayNode();
            for (GeoPoint point : points) {
                ArrayNode coordinate = objectMapper.createArrayNode();
                coordinate.add(point.getLng());
                coordinate.add(point.getLat());
                coordinates.add(coordinate);
            }
            requestBody.set("coordinates", coordinates);

            // Explicitly request GeoJSON format
            requestBody.put("format", "geojson");

            // Add some convenience options that might help with processing
            requestBody.put("instructions", false);
            requestBody.put("elevation", false);
            requestBody.put("geometry_simplify", false);

            logger.info("Making request to OpenRouteService with profile: {} and {} points", profile, points.size());
            logger.debug("Request body: {}", requestBody.toString());

            return webClient.post()
                    .uri("/directions/" + profile + "/geojson")
                    .header(HttpHeaders.AUTHORIZATION, apiKey.startsWith("Bearer ") ? apiKey : "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody.toString())
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError(), response -> response.bodyToMono(String.class)
                            .flatMap(body -> {
                                if (response.statusCode() == HttpStatus.UNAUTHORIZED ||
                                        response.statusCode() == HttpStatus.FORBIDDEN) {
                                    logger.error("API Key error: {} - {}", response.statusCode(), body);
                                    return Mono.error(new RuntimeException(
                                            "Error de autenticación con OpenRouteService. Verifica tu API key."));
                                } else if (response.statusCode() == HttpStatus.NOT_FOUND) {
                                    logger.warn("GeoJSON specific endpoint not found, will try standard endpoint");
                                    return Mono.error(new RuntimeException(
                                            "Endpoint específico no encontrado, intentando alternativa"));
                                } else {
                                    logger.error("Client error: {} - {}", response.statusCode(), body);
                                    return Mono.error(new RuntimeException("Error en los datos enviados: " + body));
                                }
                            }))
                    .onStatus(status -> status.is5xxServerError(), response -> response.bodyToMono(String.class)
                            .flatMap(body -> {
                                logger.error("Server error from OpenRouteService: {} - {}", response.statusCode(),
                                        body);
                                return Mono.error(new RuntimeException(
                                        "El servicio de rutas no está disponible en este momento. Inténtalo más tarde."));
                            }))
                    .bodyToMono(String.class)
                    .doOnError(error -> {
                        // If it's the specific error about the endpoint not being found, try the
                        // standard endpoint
                        if (error.getMessage() != null && error.getMessage().contains("intentando alternativa")) {
                            logger.info("Falling back to standard endpoint without /geojson suffix");
                        } else {
                            logger.error("Error from OpenRouteService API", error);
                        }
                    });
        } catch (Exception e) {
            logger.error("Error preparing OpenRouteService API request", e);
            return Mono.error(e);
        }
    }

    private CalculatedRouteDTO processDirectionsResponse(String responseJson, VehicleType vehicleType) {
        try {
            logger.debug("Processing response from OpenRouteService");

            // Log the complete response for debugging
            if (logger.isTraceEnabled()) {
                logger.trace("Response JSON: {}", responseJson);
            } else {
                logger.debug("Response JSON snippet: {}",
                        responseJson.length() > 200 ? responseJson.substring(0, 200) + "..." : responseJson);
            }

            JsonNode rootNode = objectMapper.readTree(responseJson);

            // Verify the basic GeoJSON structure
            if (!rootNode.has("features") || !rootNode.get("features").isArray()
                    || rootNode.get("features").size() == 0) {
                logger.error("Invalid GeoJSON response format. Response: {}", responseJson);
                return new CalculatedRouteDTO("Formato de respuesta inválido desde OpenRouteService");
            }

            JsonNode feature = rootNode.get("features").get(0);
            JsonNode properties = feature.path("properties");
            JsonNode summary = properties.path("summary");
            double distanceKm;
            double durationMinutes;

            // Check if summary section exists
            if (summary.isMissingNode() || summary.isEmpty()) {
                logger.error("Missing summary section in response. Properties structure: {}", properties);
                return new CalculatedRouteDTO("Missing route summary data in response");
            }            // Extract distance in km (API returns meters)
            distanceKm = summary.path("distance").asDouble() / 1000.0;
            // Round to 2 decimals
            distanceKm = Math.round(distanceKm * 100.0) / 100.0;

            // Extract duration in minutes (API returns seconds)
            durationMinutes = summary.path("duration").asDouble() / 60.0;
            // Convert to integer by rounding to nearest whole minute
            durationMinutes = Math.round(durationMinutes);

            // Extract coordinates from the geometry
            List<GeoPoint> routePoints = new ArrayList<>();

            if (feature.has("geometry") && feature.get("geometry").has("coordinates")) {
                JsonNode coordinates = feature.get("geometry").get("coordinates");

                if (coordinates.isArray()) {
                    for (JsonNode coord : coordinates) {
                        if (coord.isArray() && coord.size() >= 2) {
                            // Convert from [lng, lat] to GeoPoint(lat, lng)
                            double lng = coord.get(0).asDouble();
                            double lat = coord.get(1).asDouble();
                            routePoints.add(new GeoPoint(lat, lng));
                        }
                    }
                }
            }

            if (routePoints.isEmpty()) {
                return new CalculatedRouteDTO("Could not extract route points from response");
            }

            int durationMinutesInt = (int) Math.round(durationMinutes);
            
            logger.info("Route calculated successfully: {} points, {} km, {} min", 
                       routePoints.size(), distanceKm, durationMinutesInt);

            return new CalculatedRouteDTO(routePoints, durationMinutesInt, distanceKm, vehicleType);
        } catch (Exception e) {
            logger.error("Error processing directions response", e);
            return new CalculatedRouteDTO("Error processing route data: " + e.getMessage());
        }
    }
}
