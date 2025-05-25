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

import jakarta.servlet.ServletException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
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

        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", finalMessage);

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);

        Throwable cause = ex.getCause();
        if (cause != null && cause.getCause() instanceof IllegalArgumentException) {
            String errorMessage = cause.getCause().getMessage();
            response.put("message", "Error de formato: " + errorMessage);
        } else {
            response.put("message", "Error al procesar el JSON: El formato de la solicitud es inválido");
        }

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

    // Handler for coordinate errors and other IllegalArgumentException
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Datos inválidos: " + ex.getMessage());

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
}