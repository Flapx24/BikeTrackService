package com.example.demo.enums;

public enum VehicleType {
    BICYCLE("cycling-regular"),
    CAR("driving-car"),
    WALKING("foot-walking");

    private final String profile;

    VehicleType(String profile) {
        this.profile = profile;
    }

    public String getProfile() {
        return profile;
    }

    public static VehicleType fromString(String text) {
        for (VehicleType type : VehicleType.values()) {
            if (type.name().equalsIgnoreCase(text)) {
                return type;
            }
        }
        // Default to bicycle if not found
        return BICYCLE;
    }
}