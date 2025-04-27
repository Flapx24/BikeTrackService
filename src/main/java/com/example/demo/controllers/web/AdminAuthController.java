package com.example.demo.controllers.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.dtos.UserDTO;
import com.example.demo.entities.User;
import com.example.demo.enums.Role;
import com.example.demo.services.UserService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class AdminAuthController {
    
    @Autowired
    @Qualifier("userService")
    private UserService userService;
    
    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error, Model model,
            HttpServletRequest request, RedirectAttributes redirectAttributes) {
        if (error != null) {
            String attemptedUserEmail = (String) request.getSession().getAttribute("attemptedUserEmail");
            User attemptedUser = userService.findByEmail(attemptedUserEmail);
            
            if (attemptedUser == null) {
                redirectAttributes.addFlashAttribute("notExistingUser", "El usuario introducido no existe");
            } else {
                UserDTO userDTO = new UserDTO(attemptedUser);
                redirectAttributes.addFlashAttribute("attemptedUser", userDTO);
                
                if (!attemptedUser.getActive()) {
                    redirectAttributes.addFlashAttribute("userNotActivated", "El usuario no está activado");
                } else {
                    redirectAttributes.addFlashAttribute("error", "Credenciales incorrectas. Inténtalo nuevamente.");
                }
            }
            return "redirect:/login";
        }
        return "auth/login";
    }
}
