package com.example.demo.controllers.api;

import com.example.demo.dtos.CalculatedRouteDTO;
import com.example.demo.dtos.RouteCalculationRequestDTO;
import com.example.demo.models.GeoPoint;
import com.example.demo.enums.VehicleType;
import com.example.demo.services.RouteCalculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/route-calculation")
public class RouteCalculationController {

    @Autowired
    private RouteCalculationService routeCalculationService;

    /**
     * Calculates an optimal route between the provided points
     * 
     * @param authHeader  JWT authorization token
     * @param request Request containing points and vehicle type
     * @return Calculated route with points, distance and time
     */
    @PostMapping("/calculate")
    public ResponseEntity<?> calculateRoute(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody RouteCalculationRequestDTO request) {

        try {
            // Additional validation for better error messages
            if (request == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "El cuerpo de la solicitud es obligatorio"));
            }

            List<GeoPoint> points = request.getPoints();
            VehicleType vehicleType = request.getVehicleType();

            // Validate points
            if (points == null || points.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Los puntos de la ruta son obligatorios"));
            }

            if (points.size() < 2) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Se requieren al menos 2 puntos para calcular una ruta"));
            }

            if (points.size() > 50) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "No se pueden calcular rutas con más de 50 puntos"));
            }

            // Validate vehicleType is required
            if (vehicleType == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", String.format("El tipo de vehículo es obligatorio. Los valores permitidos son: %s", 
                            VehicleType.getValidValues())));
            }

            // Validate each point coordinates
            for (int i = 0; i < points.size(); i++) {
                GeoPoint point = points.get(i);
                if (point == null) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "message", "El punto " + (i + 1) + " es nulo"));
                }
                if (point.getLat() == null || point.getLng() == null) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "message", "El punto " + (i + 1) + " tiene coordenadas nulas"));
                }
                if (point.getLat() < -90 || point.getLat() > 90) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "message", "El punto " + (i + 1) + " tiene latitud inválida: " + point.getLat() + ". Debe estar entre -90 y 90"));
                }
                if (point.getLng() < -180 || point.getLng() > 180) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "success", false,
                            "message", "El punto " + (i + 1) + " tiene longitud inválida: " + point.getLng() + ". Debe estar entre -180 y 180"));
                }
            }

            CalculatedRouteDTO calculatedRoute = routeCalculationService.calculateRoute(points, vehicleType);

            if (!calculatedRoute.isSuccess()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", calculatedRoute.getMessage()));
            }

            Map<String, Object> calculatedRouteData = Map.of(
                    "routePoints", calculatedRoute.getRoutePoints(),
                    "totalDistanceKm", calculatedRoute.getTotalDistanceKm(),
                    "estimatedTimeMinutes", calculatedRoute.getEstimatedTimeMinutes(),
                    "vehicleType", calculatedRoute.getVehicleType()
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Ruta calculada con éxito",
                    "data", calculatedRouteData));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error al calcular la ruta: " + e.getMessage()));
        }
    }
}