package com.example.demo.security;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import com.example.demo.enums.VehicleType;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import jakarta.servlet.ServletException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        
        // Check for specific vehicleType validation errors
        boolean hasVehicleTypeError = ex.getBindingResult().getFieldErrors().stream()
            .anyMatch(error -> "vehicleType".equals(error.getField()));
            
        if (hasVehicleTypeError) {
            // Provide specific message for vehicleType field
            response.put("message", 
                String.format("El tipo de vehículo es obligatorio. Los valores permitidos son: %s", 
                VehicleType.getValidValues()));
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        // Generic validation error handling
        StringBuilder messageBuilder = new StringBuilder("Error de validación: ");

        // Collect all field errors and concatenate them
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            messageBuilder.append(fieldName).append(" (").append(errorMessage).append("), ");
        });

        // Remove the last comma and space
        String finalMessage = messageBuilder.toString();
        if (finalMessage.endsWith(", ")) {
            finalMessage = finalMessage.substring(0, finalMessage.length() - 2);
        }

        response.put("message", finalMessage);
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);

        // Check for specific VehicleType enum errors
        Throwable rootCause = getRootCause(ex);
        
        if (rootCause instanceof IllegalArgumentException) {
            String errorMessage = rootCause.getMessage();
            if (errorMessage != null && errorMessage.contains("Tipo de vehículo")) {
                response.put("message", errorMessage);
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        }
        
        // Check for JSON format errors with enums
        if (ex.getCause() instanceof InvalidFormatException) {
            InvalidFormatException ife = (InvalidFormatException) ex.getCause();
            if (ife.getTargetType() != null && ife.getTargetType().equals(VehicleType.class)) {
                String invalidValue = ife.getValue() != null ? ife.getValue().toString() : "null";
                response.put("message", 
                    String.format("Tipo de vehículo inválido: '%s'. Los valores permitidos son: %s", 
                    invalidValue, VehicleType.getValidValues()));
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        }
        
        // Check for missing required fields in JSON
        if (ex.getMessage() != null && ex.getMessage().contains("Required request body is missing")) {
            response.put("message", "El cuerpo de la solicitud es obligatorio");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        
        // Generic JSON parsing error
        response.put("message", "Error al procesar el JSON: El formato de la solicitud es inválido");
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Handler for missing request parameters
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Parámetro requerido faltante: " + ex.getParameterName());

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Handler for VehicleType errors and other IllegalArgumentException
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        
        String message = ex.getMessage();
        if (message != null && message.contains("Tipo de vehículo")) {
            response.put("message", message);
        } else {
            response.put("message", "Datos inválidos: " + message);
        }

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Handler for authentication errors
    @ExceptionHandler({ AuthenticationException.class, AccessDeniedException.class })
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(Exception ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Se requiere autenticación para acceder a este recurso");

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    // General handler for unhandled exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAllExceptions(Exception ex) {
        // No manejar excepciones que ya tienen manejadores específicos
        if (ex instanceof MethodArgumentNotValidException ||
                ex instanceof HttpMessageNotReadableException ||
                ex instanceof MissingServletRequestParameterException ||
                ex instanceof IllegalArgumentException ||
                ex instanceof AuthenticationException ||
                ex instanceof AccessDeniedException ||
                ex instanceof ServletException) {
            throw new RuntimeException("Esta excepción debe ser manejada por su propio handler", ex);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);

        // Consolidate error information into message field
        String errorMessage = ex.getMessage();
        if (errorMessage != null && !errorMessage.isEmpty()) {
            response.put("message", "Error interno del servidor: " + errorMessage);
        } else {
            response.put("message", "Error interno del servidor");
        }

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Helper method to get the root cause of an exception
     */
    private Throwable getRootCause(Throwable throwable) {
        Throwable rootCause = throwable;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }
        return rootCause;
    }
}