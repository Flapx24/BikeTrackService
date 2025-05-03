package com.example.demo.controllers.web;

import java.util.List;

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

import com.example.demo.dtos.RouteDTO;
import com.example.demo.dtos.UserDTO;
import com.example.demo.entities.Route;
import com.example.demo.entities.User;
import com.example.demo.services.RouteService;
import com.example.demo.services.UserService;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminRoutesController {
    
    @Autowired
    @Qualifier("userService")
    private UserService userService;
    
    @Autowired
    @Qualifier("routeService")
    private RouteService routeService;
    
    @GetMapping("/routes")
    public String listRoutes(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String title,
            @RequestParam(required = false, defaultValue = "none") String sort,
            Model model) {
        
        List<RouteDTO> filteredRoutes = routeService.getFilteredRoutes(city, title, sort);
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            User currentUser = (User) auth.getPrincipal();
            model.addAttribute("currentUser", new UserDTO(currentUser));
        }
        
        model.addAttribute("routes", filteredRoutes);
        model.addAttribute("cityFilter", city != null ? city : "");
        model.addAttribute("titleFilter", title != null ? title : "");
        model.addAttribute("sortBy", sort);
        
        return "admin/routes/list";
    }
    
    @PostMapping("/deleteRoute")
    public String deleteRoute(
            @RequestParam Long routeId, 
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String title,
            @RequestParam(required = false, defaultValue = "none") String sort,
            RedirectAttributes redirectAttributes) {
        
        Route route = routeService.findById(routeId);
        
        if (route != null) {
            boolean deleted = routeService.deleteRoute(routeId);
            
            if (deleted) {
                redirectAttributes.addFlashAttribute("message", 
                        "Ruta eliminada correctamente.");
            } else {
                redirectAttributes.addFlashAttribute("error", "No se pudo eliminar la ruta.");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Ruta no encontrada.");
        }
        
        StringBuilder redirectUrl = new StringBuilder("/admin/routes");
        boolean hasParam = false;
        
        if (city != null && !city.isEmpty()) {
            redirectUrl.append(hasParam ? "&" : "?").append("city=").append(city);
            hasParam = true;
        }
        
        if (title != null && !title.isEmpty()) {
            redirectUrl.append(hasParam ? "&" : "?").append("title=").append(title);
            hasParam = true;
        }
        
        if (sort != null && !sort.isEmpty() && !sort.equals("none")) {
            redirectUrl.append(hasParam ? "&" : "?").append("sort=").append(sort);
        }
        
        return "redirect:" + redirectUrl.toString();
    }
}