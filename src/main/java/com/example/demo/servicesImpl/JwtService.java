package com.example.demo.servicesImpl;

import com.example.demo.entities.User;
import com.example.demo.services.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service("jwtService")
public class JwtService {

    @Autowired
    private String jwtSecret;

    @Autowired
    private Long jwtExpirationShort;

    @Autowired
    private Long jwtExpirationLong;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    @Autowired
    private UserService userService;

    /**
     * Generates a JWT token with standard (short) duration
     */
    public String generateToken(UserDetails user) {
        return generateToken(user, false);
    }

    /**
     * Generates a JWT token with a variable duration based on the "remember me"
     * option.
     * 
     * @param user User details
     * @param rememberMe If true, generates a long-lived token
     * @return Token JWT
     */
    public String generateToken(UserDetails user, boolean rememberMe) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("remember", rememberMe);

        Long userId = ((User) user).getId();

        long expiration = rememberMe ? jwtExpirationLong : jwtExpirationShort;

        return Jwts.builder()
                .claims(claims)
                .subject(userId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(secretKey)
                .compact();
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Get user from token subject
     */
    public User getUser(String token) {
        String userId = extractUserId(cleanToken(token));
        return userService.findById(Long.parseLong(userId));
    }

    public String extractUserId(String token) {
        return getClaims(cleanToken(token)).getSubject();
    }

    public boolean isRememberMeToken(String token) {
        Claims claims = getClaims(cleanToken(token));
        return claims.get("remember", Boolean.class) != null &&
                claims.get("remember", Boolean.class);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String userId = extractUserId(token);
        return userId.equals(String.valueOf(((User) userDetails).getId())) && !isTokenExpired(token);
    }

    /**
     * Checks if a token has expired
     * 
     * @param token The JWT token to verify
     * @return true if the token has expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        try {
            return getClaims(cleanToken(token)).getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public boolean isAdmin(String token) {
        return hasRole(token, "ROLE_ADMIN");
    }

    public boolean isUser(String token) {
        return hasRole(token, "ROLE_USER");
    }

    private boolean hasRole(String token, String role) {
        return getUser(token).getRole().name().equals(role);
    }

    private String cleanToken(String token) {
        return token.replace("Bearer ", "");
    }
}