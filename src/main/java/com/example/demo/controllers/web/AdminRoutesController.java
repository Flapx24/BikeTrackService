package com.example.demo.controllers.web;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.dtos.RouteDTO;
import com.example.demo.dtos.UserDTO;
import com.example.demo.entities.Route;
import com.example.demo.entities.User;
import com.example.demo.enums.RouteDetailLevel;
import com.example.demo.models.GeoPoint;
import com.example.demo.services.RouteService;
import com.example.demo.services.UserService;
import com.example.demo.upload.StorageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    @Autowired
    @Qualifier("storageService")
    private StorageService storageService;

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

    @GetMapping("/routes/create")
    public String showCreateForm(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String title,
            @RequestParam(required = false, defaultValue = "none") String sort,
            Model model) {
        RouteDTO route = new RouteDTO();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            User currentUser = (User) auth.getPrincipal();
            model.addAttribute("currentUser", new UserDTO(currentUser));
        }

        model.addAttribute("route", route);
        model.addAttribute("pageTitle", "Crear nueva ruta");
        model.addAttribute("cityFilter", city);
        model.addAttribute("titleFilter", title);
        model.addAttribute("sortBy", sort);

        return "admin/routes/form";
    }

    @GetMapping("/routes/update/{id}")
    public String showUpdateForm(
            @PathVariable Long id,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String title,
            @RequestParam(required = false, defaultValue = "none") String sort,
            Model model,
            RedirectAttributes redirectAttributes) {
        Route route = routeService.findById(id);

        if (route == null) {
            redirectAttributes.addFlashAttribute("error", "La ruta solicitada no existe.");
            return "redirect:/admin/routes";
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            User currentUser = (User) auth.getPrincipal();
            model.addAttribute("currentUser", new UserDTO(currentUser));
        }

        RouteDTO routeDTO = RouteDTO.fromEntity(route, RouteDetailLevel.FULL);

        model.addAttribute("route", routeDTO);
        model.addAttribute("pageTitle", "Actualizar ruta");
        model.addAttribute("cityFilter", city);
        model.addAttribute("titleFilter", title);
        model.addAttribute("sortBy", sort);

        return "admin/routes/form";
    }

    @PostMapping("/routes/create")
    public String createRoute(
            @ModelAttribute("route") RouteDTO routeDTO,
            BindingResult bindingResult,
            @RequestParam(required = false) String coordinatesInput,
            @RequestParam(required = false) String imageUrlsInput,
            @RequestParam(required = false) List<MultipartFile> imageFiles,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            if (coordinatesInput != null && !coordinatesInput.isEmpty()) {
                ObjectMapper mapper = new ObjectMapper();
                List<GeoPoint> coordinates = mapper.readValue(coordinatesInput, new TypeReference<List<GeoPoint>>() {
                });
                routeDTO.setRoutePoints(coordinates);
            }
        } catch (JsonProcessingException e) {
            bindingResult.rejectValue("routePoints", "error.routePoints", "Formato de coordenadas inválido");
        }

        validateRouteData(routeDTO, bindingResult);

        if (bindingResult.hasErrors()) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
                User currentUser = (User) auth.getPrincipal();
                model.addAttribute("currentUser", new UserDTO(currentUser));
            }

            model.addAttribute("route", routeDTO);
            model.addAttribute("pageTitle", "Crear nueva ruta");
            return "admin/routes/form";
        }

        List<String> imageUrls = new ArrayList<>();

        if (imageUrlsInput != null && !imageUrlsInput.isEmpty()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                imageUrls = mapper.readValue(imageUrlsInput, new TypeReference<List<String>>() {
                });
            } catch (JsonProcessingException e) {
                imageUrls = new ArrayList<>();
            }
        }
        routeDTO.setImageUrls(imageUrls);

        routeDTO.setAverageReviewScore(0.0);

        Route route = routeDTO.toEntity();
        Route savedRoute = routeService.saveRoute(route);

        // Process new uploaded images with the route ID
        if (imageFiles != null && !imageFiles.isEmpty()) {
            List<MultipartFile> nonEmptyFiles = imageFiles.stream()
                    .filter(file -> file != null && !file.isEmpty())
                    .collect(Collectors.toList());

            if (!nonEmptyFiles.isEmpty()) {
                try {
                    List<String> newImageUrls = storageService.storeMultiple(nonEmptyFiles, "route",
                            savedRoute.getId());

                    if (savedRoute.getImageUrls() == null) {
                        savedRoute.setImageUrls(new ArrayList<>());
                    }

                    savedRoute.getImageUrls().addAll(newImageUrls);

                    savedRoute = routeService.saveRoute(savedRoute);
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("message",
                            "Ruta creada pero hubo un problema al subir las imágenes.");
                    return "redirect:/admin/routes";
                }
            }
        }

        redirectAttributes.addFlashAttribute("message", "Ruta creada correctamente.");

        return "redirect:/admin/routes";
    }

    @PostMapping("/routes/update")
    public String updateRoute(
            @ModelAttribute("route") RouteDTO routeDTO,
            BindingResult bindingResult,
            @RequestParam(required = false) String coordinatesInput,
            @RequestParam(required = false) List<String> existingImageUrls,
            @RequestParam(required = false) List<String> deletedImageUrls,
            @RequestParam(required = false) List<MultipartFile> imageFiles,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            if (coordinatesInput != null && !coordinatesInput.isEmpty()) {
                ObjectMapper mapper = new ObjectMapper();
                List<GeoPoint> coordinates = mapper.readValue(coordinatesInput, new TypeReference<List<GeoPoint>>() {
                });
                routeDTO.setRoutePoints(coordinates);
            }
        } catch (JsonProcessingException e) {
            bindingResult.rejectValue("routePoints", "error.routePoints", "Formato de coordenadas inválido");
        }

        validateRouteData(routeDTO, bindingResult);

        if (bindingResult.hasErrors()) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
                User currentUser = (User) auth.getPrincipal();
                model.addAttribute("currentUser", new UserDTO(currentUser));
            }
            model.addAttribute("route", routeDTO);
            model.addAttribute("pageTitle", "Actualizar ruta");
            return "admin/routes/form";
        }

        Route existingRoute = routeService.findById(routeDTO.getId());
        if (existingRoute == null) {
            redirectAttributes.addFlashAttribute("error", "La ruta que intenta actualizar no existe.");
            return "redirect:/admin/routes";
        }
        // Process deleted images first
        if (deletedImageUrls != null && !deletedImageUrls.isEmpty()) {

            for (String imageUrl : deletedImageUrls) {
                try {
                    // Extract the filename from the URL
                    String filename = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
                    String entityType = "route";
                    String[] parts = filename.split("_");

                    if (parts.length >= 2) {
                        Long entityId = Long.parseLong(parts[0]);
                        Integer position = Integer.parseInt(parts[1].split("\\.")[0]);

                        // Delete the file from the file system
                        storageService.delete(entityType, entityId, position);

                    } else {

                    }
                } catch (Exception e) {

                }
            }
        }

        List<String> imageUrls = existingImageUrls != null ? existingImageUrls : new ArrayList<>();

        routeDTO.setImageUrls(imageUrls);
        Route route = routeDTO.toEntity(existingRoute);

        // Process new images with the path ID

        Route updatedRoute = routeService.saveRoute(route);
        if (imageFiles != null && !imageFiles.isEmpty()) {
            List<MultipartFile> nonEmptyFiles = imageFiles.stream()
                    .filter(file -> file != null && !file.isEmpty())
                    .collect(Collectors.toList());

            if (!nonEmptyFiles.isEmpty()) {
                try {

                    List<String> newImageUrls = storageService.storeMultiple(nonEmptyFiles, "route",
                            updatedRoute.getId());

                    if (updatedRoute.getImageUrls() == null) {
                        updatedRoute.setImageUrls(new ArrayList<>());
                    }

                    updatedRoute.getImageUrls().addAll(newImageUrls);
                    updatedRoute = routeService.saveRoute(updatedRoute);
                } catch (Exception e) {

                    redirectAttributes.addFlashAttribute("error",
                            "Se guardó la ruta pero hubo un problema al subir las imágenes.");
                    return "redirect:/admin/routes";

                }
            } else {

            }
        }

        redirectAttributes.addFlashAttribute("message", "Ruta actualizada correctamente.");
        return "redirect:/admin/routes";
    }

    private void validateRouteData(RouteDTO routeDTO, BindingResult bindingResult) {
        if (routeDTO.getTitle() == null || routeDTO.getTitle().trim().isEmpty()) {
            bindingResult.rejectValue("title", "NotBlank", "El título es obligatorio");
        }

        if (routeDTO.getCity() == null || routeDTO.getCity().trim().isEmpty()) {
            bindingResult.rejectValue("city", "NotBlank", "La ciudad es obligatoria");
        }

        if (routeDTO.getDescription() == null || routeDTO.getDescription().trim().isEmpty()) {
            bindingResult.rejectValue("description", "NotBlank", "La descripción es obligatoria");
        }

        if (routeDTO.getDifficulty() == null) {
            bindingResult.rejectValue("difficulty", "NotNull", "La dificultad es obligatoria");
        }

        if (routeDTO.getRoutePoints() == null || routeDTO.getRoutePoints().isEmpty()) {
            bindingResult.rejectValue("routePoints", "NotEmpty", "Debe añadir al menos un punto a la ruta");
        }
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