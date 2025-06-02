package com.example.demo.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum VehicleType {
    BICYCLE("cycling-regular", "Bicicleta"),
    CAR("driving-car", "Automóvil"),
    WALKING("foot-walking", "Caminando");

    private final String profile;
    private final String displayName;

    VehicleType(String profile, String displayName) {
        this.profile = profile;
        this.displayName = displayName;
    }

    public String getProfile() {
        return profile;
    }

    public String getDisplayName() {
        return displayName;
    }

    @JsonValue
    public String getName() {
        return this.name();
    }

    @JsonCreator
    public static VehicleType fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException(
                String.format("El tipo de vehículo es obligatorio. Los valores permitidos son: %s", 
                getValidValues())
            );
        }
        
        String normalizedText = text.trim().toUpperCase();
        
        // Try exact match first
        for (VehicleType type : VehicleType.values()) {
            if (type.name().equals(normalizedText)) {
                return type;
            }
        }
        
        // Try aliases
        switch (normalizedText) {
            case "BIKE":
            case "BICICLETA":
            case "CYCLING":
                return BICYCLE;
            case "AUTO":
            case "AUTOMOBILE":
            case "COCHE":
            case "AUTOMOVIL":
            case "DRIVING":
                return CAR;
            case "FOOT":
            case "WALK":
            case "CAMINANDO":
            case "PIE":
                return WALKING;
            default:
                throw new IllegalArgumentException(
                    String.format("Tipo de vehículo inválido: '%s'. Los valores permitidos son: %s", 
                    text, getValidValues())
                );
        }
    }

    public static String getValidValues() {
        StringBuilder sb = new StringBuilder();
        for (VehicleType type : VehicleType.values()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(type.name()).append(" (").append(type.displayName).append(")");
        }
        return sb.toString();
    }

    public static boolean isValid(String text) {
        try {
            fromString(text);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}