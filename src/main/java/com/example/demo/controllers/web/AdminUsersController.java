package com.example.demo.controllers.web;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.dtos.UserDTO;
import com.example.demo.entities.User;
import com.example.demo.enums.Role;
import com.example.demo.services.UserService;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminUsersController {
    
    @Autowired
    @Qualifier("userService")
    private UserService userService;
    
    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> userEntities = userService.getAllUsers();
        
        List<UserDTO> users = userEntities.stream()
                .filter(user -> user.getRole() != Role.ROLE_ADMIN)
                .map(UserDTO::new)
                .collect(Collectors.toList());
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            User currentUser = (User) auth.getPrincipal();
            model.addAttribute("currentUser", new UserDTO(currentUser));
        }
        
        model.addAttribute("users", users);
        return "dashboard/users_list";
    }
    
    @PostMapping("/toggleUserStatus")
    public String toggleUserStatus(@RequestParam("userId") Long userId, RedirectAttributes redirectAttributes) {
        User user = userService.findById(userId);
        
        if (user != null) {
            user.setActive(!user.getActive());
            userService.saveUser(user);
            
            String status = user.getActive() ? "activado" : "desactivado";
            redirectAttributes.addFlashAttribute("message", 
                    "Usuario " + user.getUsername() + " " + status + " correctamente.");
        } else {
            redirectAttributes.addFlashAttribute("error", "Usuario no encontrado.");
        }
        
        return "redirect:/admin/users";
    }
}