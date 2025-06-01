package com.example.demo.servicesImpl;

import com.example.demo.entities.User;
import com.example.demo.enums.Role;
import com.example.demo.repositories.UserRepository;
import com.example.demo.services.UserService;
import com.example.demo.upload.StorageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service("userAuthService")
public class UserAuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    @Qualifier("userRepository")
    private UserRepository userRepository;

    @Autowired
    @Qualifier("userService")
    private UserService userService;

    @Autowired
    @Qualifier("jwtService")
    private JwtService jwtService;

    @Autowired
    @Qualifier("storageService")
    private StorageService storageService;

    /**
     * Manual login with email and password
     * Generates a token based on the rememberMe option
     * 
     * @return Map with user's name and token
     */
    public Map<String, Object> login(String email, String password, boolean rememberMe) {
        // Normalize email to lowercase
        if (email != null) {
            email = email.toLowerCase();
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));

        User user = (User) authentication.getPrincipal();

        if (!user.getRole().name().equals("ROLE_USER")) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "Acceso denegado. Solo usuarios con rol USER pueden acceder a la API.");
            return result;
        }

        String token = jwtService.generateToken(user, rememberMe);

        Map<String, Object> result = new HashMap<>();
        result.put("name", user.getName());
        result.put("token", token);
        result.put("success", true);
        result.put("message", "Login exitoso");

        return result;
    }

    /**
     * Short version without rememberMe
     */
    public Map<String, Object> login(String email, String password) {
        return login(email, password, false);
    }

    /**
     * Verifies if a token is valid
     * 
     * @param token The JWT token to verify
     * @return true if the token is valid, false otherwise
     */
    public boolean isTokenValid(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        try {
            String cleanToken = token.replace("Bearer ", "");

            if (jwtService.isTokenExpired(cleanToken)) {
                return false;
            }

            User user = jwtService.getUser(cleanToken);
            if (user == null) {
                return false;
            }

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Login with existing token
     * 
     * @param token The JWT token
     * @return Map with user's name and token if it's valid
     */
    public Map<String, Object> loginWithToken(String token) {
        Map<String, Object> result = new HashMap<>();

        if (!isTokenValid(token)) {
            result.put("success", false);
            result.put("message", "Token inválido o expirado");
            return result;
        }

        String cleanToken = token.replace("Bearer ", "");
        User user = jwtService.getUser(cleanToken);

        result.put("name", user.getName());
        result.put("token", token);
        result.put("success", true);
        result.put("message", "Token válido");

        return result;
    }

    public Map<String, Object> register(User user) throws RuntimeException {
        Map<String, Object> result = new HashMap<>();
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "El nombre de usuario es obligatorio");
            return result;
        }

        user.setUsername(user.getUsername().toLowerCase());

        if (user.getEmail() != null) {
            user.setEmail(user.getEmail().toLowerCase());
        }

        if (userService.existsByUsernameIgnoreCase(user.getUsername())) {
            result.put("success", false);
            result.put("message", "El nombre de usuario ya está en uso");
            return result;
        }

        if (userService.existsByEmailIgnoreCase(user.getEmail())) {
            result.put("success", false);
            result.put("message", "El correo electrónico ya está registrado");
            return result;
        }

        String password = user.getPassword();
        if (password == null || password.length() < 8) {
            result.put("success", false);
            result.put("message", "La contraseña debe tener al menos 8 caracteres");
            return result;
        }

        if (!password.matches(".*[A-Z].*")) {
            result.put("success", false);
            result.put("message", "La contraseña debe contener al menos una letra mayúscula");
            return result;
        }

        if (!password.matches(".*[a-z].*")) {
            result.put("success", false);
            result.put("message", "La contraseña debe contener al menos una letra minúscula");
            return result;
        }

        if (!password.matches(".*\\d.*")) {
            result.put("success", false);
            result.put("message", "La contraseña debe contener al menos un número");
            return result;
        }

        if (!password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
            result.put("success", false);
            result.put("message", "La contraseña debe contener al menos un carácter especial");
            return result;
        }

        user.setRole(Role.ROLE_USER);
        user.setActive(true);

        String randomImageUrl = storageService.getRandomUserImage();
        user.setImageUrl(randomImageUrl);

        userService.saveUser(user);

        result.put("success", true);
        result.put("message", "Registro exitoso");
        result.put("imageUrl", user.getImageUrl());

        return result;
    }
}