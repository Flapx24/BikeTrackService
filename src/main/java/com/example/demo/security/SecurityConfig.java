package com.example.demo.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class SecurityConfig {

    @Autowired
    @Qualifier("jwtFilter")
    private JwtFilter jwtFilter;
    
    @Autowired
    private CustomAuthenticationFailureHandler failureHandler;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
    
    @Bean
    public AuthenticationEntryPoint apiAuthenticationEntryPoint() {
        return new AuthenticationEntryPoint() {
            @Override
            public void commence(HttpServletRequest request, HttpServletResponse response,
                    AuthenticationException authException) throws IOException, ServletException {
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                
                Map<String, Object> body = new HashMap<>();
                body.put("success", false);
                body.put("message", "Se requiere autenticación para acceder a este recurso");
                
                response.getOutputStream().println(objectMapper.writeValueAsString(body));
            }
        };
    }
    
    @Bean
    public AccessDeniedHandler apiAccessDeniedHandler() {
        return new AccessDeniedHandler() {
            @Override
            public void handle(HttpServletRequest request, HttpServletResponse response,
                    AccessDeniedException accessDeniedException) throws IOException, ServletException {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                
                Map<String, Object> body = new HashMap<>();
                body.put("success", false);
                body.put("message", "No tiene permisos para acceder a este recurso");
                
                response.getOutputStream().println(objectMapper.writeValueAsString(body));
            }
        };
    }
    
    @Bean
    @Order(1) // Higher priority for API routes
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http
            // Specific configuration for API routes
            .securityMatcher("/api/**")
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public API routes
                .requestMatchers("/api/auth/**").permitAll()
                
                // Routes for authenticated users
                .requestMatchers("/api/**").hasRole("USER")
                
                .anyRequest().authenticated()
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(apiAuthenticationEntryPoint())
                .accessDeniedHandler(apiAccessDeniedHandler())
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
            
        return http.build();
    }

    @Bean
    @Order(2) // Lower priority for web routes
    public SecurityFilterChain webFilterChain(HttpSecurity http) throws Exception {
        http
            // Specific configuration for web routes (everything except /api/**)
            .securityMatcher(new AntPathRequestMatcher("/**")) 
            .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
            .authorizeHttpRequests(auth -> auth
                // Public web routes
                .requestMatchers("/", "/auth/**", "/login", "/logout", 
                                "/css/**", "/js/**", "/images/**", "/assets/**", "/webjars/**").permitAll()
                
                // Routes for admin users
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                .anyRequest().authenticated()
            )
            // Login form configuration
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("email")
                .defaultSuccessUrl("/admin/home", true)
                .failureHandler(failureHandler)
                .permitAll()
            )
            // Logout configuration
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );
            
        return http.build();
    }
}