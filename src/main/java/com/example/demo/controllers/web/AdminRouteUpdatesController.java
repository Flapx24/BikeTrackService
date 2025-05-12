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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.dtos.RouteUpdateDTO;
import com.example.demo.dtos.UserDTO;
import com.example.demo.entities.Route;
import com.example.demo.entities.RouteUpdate;
import com.example.demo.entities.User;
import com.example.demo.services.RouteService;
import com.example.demo.services.RouteUpdateService;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminRouteUpdatesController {

    @Autowired
    @Qualifier("routeUpdateService")
    private RouteUpdateService routeUpdateService;

    @Autowired
    @Qualifier("routeService")
    private RouteService routeService;

    /**
     * Show route updates for a specific route
     * 
     * @param routeId            Route ID for which updates are shown
     * @param model              Spring model
     * @param redirectAttributes For flash messages
     * @return View name
     */
    @GetMapping("/routes/{routeId}/updates")
    public String showRouteUpdates(@PathVariable Long routeId, Model model, RedirectAttributes redirectAttributes) {
        Route route = routeService.findById(routeId);

        if (route == null) {
            redirectAttributes.addFlashAttribute("error", "La ruta solicitada no existe.");
            return "redirect:/admin/routes";
        }

        List<RouteUpdate> routeUpdates = routeUpdateService.findByRouteId(routeId);
        List<RouteUpdateDTO> routeUpdateDTOs = routeUpdates.stream()
                .map(RouteUpdateDTO::new)
                .collect(Collectors.toList());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            User currentUser = (User) auth.getPrincipal();
            model.addAttribute("currentUser", new UserDTO(currentUser));
        }

        model.addAttribute("route", route);
        model.addAttribute("routeUpdates", routeUpdateDTOs);

        return "admin/routes/updates";
    }

    /**
     * Delete a route update
     * 
     * @param updateId           Route update ID to delete
     * @param routeId            Route ID associated with the update
     * @param redirectAttributes For flash messages
     * @return Redirect to route updates page
     */
    @PostMapping("/routeUpdates/delete")
    public String deleteRouteUpdate(@RequestParam Long updateId, @RequestParam Long routeId,
            RedirectAttributes redirectAttributes) {
        boolean deleted = routeUpdateService.deleteRouteUpdate(updateId);

        if (deleted) {
            redirectAttributes.addFlashAttribute("message", "Actualización de ruta eliminada con éxito");
        } else {
            redirectAttributes.addFlashAttribute("error", "No se pudo eliminar la actualización de ruta");
        }

        return "redirect:/admin/routes/" + routeId + "/updates";
    }

    /**
     * Show route updates management
     * 
     * @param routeId            Route ID for which updates are managed
     * @param model              Spring model
     * @param redirectAttributes For flash messages
     * @return View name
     */
    @GetMapping("/routes/{routeId}/route-updates-management")
    public String showRouteUpdatesManagement(@PathVariable Long routeId, Model model,
            RedirectAttributes redirectAttributes) {
        // Redirect to the unified endpoint
        return "redirect:/admin/routes/" + routeId + "/updates";
    }
}
