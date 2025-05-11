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

import com.example.demo.dtos.UserDTO;
import com.example.demo.dtos.WorkshopDTO;
import com.example.demo.entities.User;
import com.example.demo.entities.Workshop;
import com.example.demo.models.GeoPoint;
import com.example.demo.services.UserService;
import com.example.demo.services.WorkshopService;
import com.example.demo.upload.StorageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    @Autowired
    @Qualifier("storageService")
    private StorageService storageService;

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

    @GetMapping("/workshops/create")
    public String showCreateForm(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String name,
            Model model) {
        WorkshopDTO workshop = new WorkshopDTO();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            User currentUser = (User) auth.getPrincipal();
            model.addAttribute("currentUser", new UserDTO(currentUser));
        }

        model.addAttribute("workshop", workshop);
        model.addAttribute("pageTitle", "Crear nuevo taller");
        model.addAttribute("cityFilter", city);
        model.addAttribute("nameFilter", name);

        return "admin/workshops/form";
    }

    @GetMapping("/workshops/update/{id}")
    public String showUpdateForm(
            @PathVariable Long id,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String name,
            Model model,
            RedirectAttributes redirectAttributes) {
        Workshop workshop = workshopService.findById(id);

        if (workshop == null) {
            redirectAttributes.addFlashAttribute("error", "El taller solicitado no existe.");
            return "redirect:/admin/workshops";
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            User currentUser = (User) auth.getPrincipal();
            model.addAttribute("currentUser", new UserDTO(currentUser));
        }

        WorkshopDTO workshopDTO = new WorkshopDTO(workshop);

        model.addAttribute("workshop", workshopDTO);
        model.addAttribute("pageTitle", "Actualizar taller");
        model.addAttribute("cityFilter", city);
        model.addAttribute("nameFilter", name);

        return "admin/workshops/form";
    }

    @PostMapping("/workshops/create")
    public String createWorkshop(
            @ModelAttribute("workshop") WorkshopDTO workshopDTO,
            BindingResult bindingResult,
            @RequestParam String latitude,
            @RequestParam String longitude,
            @RequestParam(required = false) String imageUrlsInput,
            @RequestParam(required = false) List<MultipartFile> imageFiles,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            double lat = Double.parseDouble(latitude);
            double lng = Double.parseDouble(longitude);
            workshopDTO.setCoordinates(new GeoPoint(lat, lng));
        } catch (NumberFormatException e) {
            bindingResult.rejectValue("coordinates", "error.coordinates", "Coordenadas inválidas");
        }

        if (workshopDTO.getName() == null || workshopDTO.getName().trim().isEmpty()) {
            bindingResult.rejectValue("name", "NotBlank", "El nombre es obligatorio");
        }

        if (workshopDTO.getCity() == null || workshopDTO.getCity().trim().isEmpty()) {
            bindingResult.rejectValue("city", "NotBlank", "La ciudad es obligatoria");
        }

        if (workshopDTO.getAddress() == null || workshopDTO.getAddress().trim().isEmpty()) {
            bindingResult.rejectValue("address", "NotBlank", "La dirección es obligatoria");
        }

        if (workshopDTO.getCoordinates() == null) {
            bindingResult.rejectValue("coordinates", "NotNull", "Las coordenadas son obligatorias");
        }

        if (bindingResult.hasErrors()) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
                User currentUser = (User) auth.getPrincipal();
                model.addAttribute("currentUser", new UserDTO(currentUser));
            }

            model.addAttribute("pageTitle", "Crear nuevo taller");
            return "admin/workshops/form";
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

        workshopDTO.setImageUrls(imageUrls);

        Workshop workshop = workshopDTO.toEntity();
        Workshop savedWorkshop = workshopService.saveWorkshop(workshop);

        // Process new uploaded images with the workshop ID
        if (imageFiles != null && !imageFiles.isEmpty()) {
            List<MultipartFile> nonEmptyFiles = imageFiles.stream()
                    .filter(file -> file != null && !file.isEmpty())
                    .collect(Collectors.toList());

            if (!nonEmptyFiles.isEmpty()) {
                List<String> newImageUrls = storageService.storeMultiple(nonEmptyFiles, "workshop",
                        savedWorkshop.getId());

                if (savedWorkshop.getImageUrls() == null) {
                    savedWorkshop.setImageUrls(new ArrayList<>());
                }

                savedWorkshop.getImageUrls().addAll(newImageUrls);

                savedWorkshop = workshopService.saveWorkshop(savedWorkshop);
            }
        }

        redirectAttributes.addFlashAttribute("message", "Taller creado correctamente.");

        return "redirect:/admin/workshops";
    }

    @PostMapping("/workshops/update")
    public String updateWorkshop(
            @ModelAttribute("workshop") WorkshopDTO workshopDTO,
            BindingResult bindingResult,
            @RequestParam String latitude,
            @RequestParam String longitude,
            @RequestParam(required = false) List<String> existingImageUrls,
            @RequestParam(required = false) List<String> deletedImageUrls,
            @RequestParam(required = false) List<MultipartFile> imageFiles,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            double lat = Double.parseDouble(latitude);
            double lng = Double.parseDouble(longitude);
            workshopDTO.setCoordinates(new GeoPoint(lat, lng));
        } catch (NumberFormatException e) {
            bindingResult.rejectValue("coordinates", "error.coordinates", "Coordenadas inválidas");
        }

        if (workshopDTO.getName() == null || workshopDTO.getName().trim().isEmpty()) {
            bindingResult.rejectValue("name", "NotBlank", "El nombre es obligatorio");
        }

        if (workshopDTO.getCity() == null || workshopDTO.getCity().trim().isEmpty()) {
            bindingResult.rejectValue("city", "NotBlank", "La ciudad es obligatoria");
        }

        if (workshopDTO.getAddress() == null || workshopDTO.getAddress().trim().isEmpty()) {
            bindingResult.rejectValue("address", "NotBlank", "La dirección es obligatoria");
        }

        if (workshopDTO.getCoordinates() == null) {
            bindingResult.rejectValue("coordinates", "NotNull", "Las coordenadas son obligatorias");
        }

        Workshop existingWorkshop = workshopService.findById(workshopDTO.getId());
        if (existingWorkshop == null) {
            redirectAttributes.addFlashAttribute("error", "El taller que intenta actualizar no existe.");
            return "redirect:/admin/workshops";
        }

        // Process deleted images if any
        if (deletedImageUrls != null && !deletedImageUrls.isEmpty()) {

            for (String imageUrl : deletedImageUrls) {
                try {
                    // Extract the file name part from the URL
                    String filename = imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
                    String entityId = filename.substring(0, filename.indexOf('_'));
                    String positionStr = filename.substring(entityId.length() + 1, filename.lastIndexOf('.'));
                    int position = Integer.parseInt(positionStr);

                    // Remove the image from the file system
                    storageService.delete("workshop", existingWorkshop.getId(), position);

                } catch (Exception e) {

                }
            }
        }

        List<String> imageUrls = existingImageUrls != null ? existingImageUrls : new ArrayList<>();
        workshopDTO.setImageUrls(imageUrls);

        if (bindingResult.hasErrors()) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
                User currentUser = (User) auth.getPrincipal();
                model.addAttribute("currentUser", new UserDTO(currentUser));
            }
            model.addAttribute("workshop", workshopDTO);
            model.addAttribute("pageTitle", "Actualizar taller");
            return "admin/workshops/form";
        }

        Workshop workshop = workshopDTO.toEntity();

        Workshop updatedWorkshop = workshopService.saveWorkshop(workshop);

        // Process new uploaded images with the workshop ID
        if (imageFiles != null && !imageFiles.isEmpty() && !imageFiles.get(0).isEmpty()) {
            try {
                List<String> newImageUrls = storageService.storeMultiple(imageFiles, "workshop",
                        updatedWorkshop.getId());
                if (updatedWorkshop.getImageUrls() == null) {
                    updatedWorkshop.setImageUrls(new ArrayList<>());
                }
                updatedWorkshop.getImageUrls().addAll(newImageUrls);

                updatedWorkshop = workshopService.saveWorkshop(updatedWorkshop);
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error",
                        "Se guardó el taller pero hubo un problema al subir las imágenes.");
                return "redirect:/admin/workshops";
            }
        }

        redirectAttributes.addFlashAttribute("message", "Taller actualizado correctamente.");
        return "redirect:/admin/workshops";
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