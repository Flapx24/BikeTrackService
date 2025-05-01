package com.example.demo.upload;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {
    
    // Base location where images will be stored
    private String location = "src/main/resources/static/images";
    
    // Base URL to access images
    private String baseUrl = "/images";
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String location) {
        this.location = location;
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    public String getEntityLocation(String entityType) {
        return location + "/" + entityType.toLowerCase();
    }

    public String getEntityUrl(String entityType) {
        return baseUrl + "/" + entityType.toLowerCase();
    }
}
