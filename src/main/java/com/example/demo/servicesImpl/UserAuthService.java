package com.example.demo.servicesImpl;

import com.example.demo.entities.User;
import com.example.demo.enums.Role;
import com.example.demo.repositories.UserRepository;
import com.example.demo.services.UserService;
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

    /**
     * Manual login with email and password
     * Generates a token based on the rememberMe option
     * 
     * @return Map with user's nickname and token
     */
    public Map<String, Object> login(String email, String password, boolean rememberMe) {
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
        result.put("nickname", user.getName());
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
     * @return Map with user's nickname and token if it's valid
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

        result.put("nickname", user.getName());
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

        if (userService.findByEmail(user.getEmail()) != null) {
            result.put("success", false);
            result.put("message", "Usuario ya registrado");
            return result;
        }

        user.setRole(Role.ROLE_USER);
        user.setActive(false);

        userService.saveUser(user);

        result.put("success", true);
        result.put("message", "Registro exitoso");

        return result;
    }
}