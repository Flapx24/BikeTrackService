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
import com.example.demo.dtos.WorkshopDTO;
import com.example.demo.entities.User;
import com.example.demo.entities.Workshop;
import com.example.demo.services.UserService;
import com.example.demo.services.WorkshopService;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminWorkshopsController {
    
    @Autowired
    @Qualifier("userService")
    private UserService userService;
    
    @Autowired
    @Qualifier("workshopService")
    private WorkshopService workshopService;
    
    @GetMapping("/workshops")
    public String listWorkshops(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String name,
            Model model) {
        
        List<Workshop> allWorkshops = workshopService.findAll();
        
        List<WorkshopDTO> filteredWorkshops = allWorkshops.stream()
                .filter(workshop -> {
                    boolean cityMatch = city == null || city.isEmpty() || 
                            workshopService.normalizeString(workshop.getCity())
                                .contains(workshopService.normalizeString(city));
                    
                    boolean nameMatch = name == null || name.isEmpty() || 
                            workshopService.normalizeString(workshop.getName())
                                .contains(workshopService.normalizeString(name));
                    
                    return cityMatch && nameMatch;
                })
                .map(WorkshopDTO::new)
                .collect(Collectors.toList());
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            User currentUser = (User) auth.getPrincipal();
            model.addAttribute("currentUser", new UserDTO(currentUser));
        }
        
        model.addAttribute("workshops", filteredWorkshops);
        model.addAttribute("cityFilter", city != null ? city : "");
        model.addAttribute("nameFilter", name != null ? name : "");
        
        return "admin/workshops/list";
    }
    
    @PostMapping("/deleteWorkshop")
    public String deleteWorkshop(
            @RequestParam Long workshopId, 
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String name,
            RedirectAttributes redirectAttributes) {
        
        Workshop workshop = workshopService.findById(workshopId);
        
        if (workshop != null) {
            boolean deleted = workshopService.deleteWorkshop(workshopId);
            
            if (deleted) {
                redirectAttributes.addFlashAttribute("message", 
                        "Taller eliminado correctamente.");
            } else {
                redirectAttributes.addFlashAttribute("error", "No se pudo eliminar el taller.");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Taller no encontrado.");
        }
        
        StringBuilder redirectUrl = new StringBuilder("/admin/workshops");
        boolean hasParam = false;
        
        if (city != null && !city.isEmpty()) {
            redirectUrl.append(hasParam ? "&" : "?").append("city=").append(city);
            hasParam = true;
        }
        
        if (name != null && !name.isEmpty()) {
            redirectUrl.append(hasParam ? "&" : "?").append("name=").append(name);
        }
        
        return "redirect:" + redirectUrl.toString();
    }
}