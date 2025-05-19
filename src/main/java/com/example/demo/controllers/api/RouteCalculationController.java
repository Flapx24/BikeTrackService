package com.example.demo.controllers.api;

import com.example.demo.dtos.CalculatedRouteDTO;
import com.example.demo.dtos.RouteCalculationRequestDTO;
import com.example.demo.models.GeoPoint;
import com.example.demo.services.RouteCalculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
     * @param requestBody Request containing points and vehicle type
     * @return Calculated route with points, distance and time
     */
    @PostMapping("/calculate")
    public ResponseEntity<?> calculateRoute(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody RouteCalculationRequestDTO request) {

        try {
            List<GeoPoint> points = request.getPoints();

            if (points == null || points.size() < 2) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Se requieren al menos 2 puntos para calcular una ruta"));
            }

            if (points.size() > 50) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "No se pueden calcular rutas con más de 50 puntos"));
            }

            String vehicleType = request.getVehicleType() != null ? request.getVehicleType() : "BICYCLE";

            CalculatedRouteDTO calculatedRoute = routeCalculationService.calculateRoute(points, vehicleType);

            if (!calculatedRoute.isSuccess()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", calculatedRoute.getMessage()));
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Ruta calculada con éxito",
                    "data", calculatedRoute));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Error al calcular la ruta: " + e.getMessage()));
        }
    }
}