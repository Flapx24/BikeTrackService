package com.example.demo.controllers.api;

import com.example.demo.dtos.auth.LoginRequest;
import com.example.demo.entities.User;
import com.example.demo.services.UserService;
import com.example.demo.servicesImpl.JwtService;
import com.example.demo.servicesImpl.UserAuthService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class UserAuthController {

    @Autowired
    @Qualifier("userAuthService")
    private UserAuthService userAuthService;

    @Autowired
    @Qualifier("userService")
    private UserService userService;
    
    @Autowired
    @Qualifier("jwtService")
    private JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            Map<String, Object> result = userAuthService.register(user);
            
            if ((boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.badRequest().body(result);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "message", "Ha ocurrido un error inesperado: " + e.getMessage()
                    ));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            String email = loginRequest.getEmail();
            String password = loginRequest.getPassword();
            boolean rememberMe = loginRequest.isRememberMe();
            
            Map<String, Object> result = userAuthService.login(email, password, rememberMe);
            return ResponseEntity.ok(result);
            
        } catch (BadCredentialsException bcex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                        "success", false,
                        "message", "Correo o contraseña incorrectos. Verifica tus datos."
                    ));
        } catch (DisabledException dex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                        "success", false,
                        "message", "La cuenta no está activada. Por favor, activa tu cuenta."
                    ));
        } catch (AuthenticationException aex) {
            String errorMessage = aex.getMessage();
            if (errorMessage == null || errorMessage.isEmpty()) {
                errorMessage = "Error en la autenticación. Verifica tus credenciales.";
            }
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                        "success", false,
                        "message", errorMessage
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "success", false,
                        "message", "Ha ocurrido un problema inesperado: " + e.getMessage()
                    ));
        }
    }

    @PostMapping("/token-login")
    public ResponseEntity<?> tokenLogin(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                            "success", false,
                            "message", "Token no proporcionado o formato inválido"
                        ));
            }

            Map<String, Object> result = userAuthService.loginWithToken(authHeader);
            
            if ((boolean) result.get("success")) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(result);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "success", false,
                        "message", "Error al procesar token: " + e.getMessage()
                    ));
        }
    }
    
    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of(
                            "valid", false,
                            "message", "Token no proporcionado o formato inválido"
                        ));
            }
            
            boolean isValid = userAuthService.isTokenValid(authHeader);
            return ResponseEntity.ok(Map.of(
                "valid", isValid,
                "message", isValid ? "Token válido" : "Token inválido o expirado"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                        "valid", false,
                        "message", "Error al validar token: " + e.getMessage()
                    ));
        }
    }
}
