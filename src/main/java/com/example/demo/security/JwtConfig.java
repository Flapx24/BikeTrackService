package com.example.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class JwtConfig {

    @Value("${jwt.secret}")
    private String secret;

    // 3 hours in milliseconds (3 * 60 * 60 * 1000)
    @Value("${jwt.expiration.short:10800000}")
    private Long expirationShort;

    // 60 days in milliseconds (60 * 24 * 60 * 60 * 1000)
    @Value("${jwt.expiration.long:5184000000}")
    private Long expirationLong;

    @Bean
    public String jwtSecret() {
        if (secret == null || secret.isEmpty() || "your_jwt_secret_key".equals(secret)) {
            throw new IllegalStateException(
                "The JWT secret key is not configured correctly. " +
                "Please set a secure secret key in application.properties using the 'jwt.secret' property."
            );
        }
        return secret;
    }

    @Bean
    public Long jwtExpirationShort() {
        return expirationShort;
    }

    @Bean
    public Long jwtExpirationLong() {
        return expirationLong;
    }
}