package com.example.demo.security;

import java.io.IOException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.FlashMapManager;
import org.springframework.web.servlet.support.SessionFlashMapManager;

import com.example.demo.entities.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, 
            AccessDeniedException accessDeniedException) throws IOException, ServletException {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        FlashMap flashMap = new FlashMap();

        flashMap.put("roleError", "Solo los administradores pueden acceder a esta página.");
        
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof User) {
            User user = (User) auth.getPrincipal();
            
            flashMap.put("attemptedUserEmail", user.getEmail());
        }
        
        String targetUrl = request.getContextPath() + "/login";
        flashMap.setTargetRequestPath("/login");
        
        FlashMapManager flashMapManager = new SessionFlashMapManager();
        
        flashMapManager.saveOutputFlashMap(flashMap, request, response);
        
        response.sendRedirect(targetUrl);
    }
}