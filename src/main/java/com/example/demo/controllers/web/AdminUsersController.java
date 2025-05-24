package com.example.demo.controllers.web;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
import com.example.demo.services.UserService;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminUsersController {

    @Autowired
    @Qualifier("userService")
    private UserService userService;

    @GetMapping("/users")
    public String listUsers(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            Model model) {

        if (size <= 0 || size > 20) {
            size = 10;
        }

        if (page < 0) {
            page = 0;
        }

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<User> usersPage = userService.getFilteredUsersPaginated(username, email, pageRequest);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            User currentUser = (User) auth.getPrincipal();
            model.addAttribute("currentUser", new UserDTO(currentUser));
        }

        List<UserDTO> userDTOs = usersPage.getContent().stream()
                .map(UserDTO::new)
                .collect(Collectors.toList());

        model.addAttribute("users", userDTOs);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", usersPage.getTotalPages());
        model.addAttribute("totalItems", usersPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("usernameFilter", username != null ? username : "");
        model.addAttribute("emailFilter", email != null ? email : "");

        return "admin/users/list";
    }

    @PostMapping("/toggleUserStatus")
    public String toggleUserStatus(
            @RequestParam Long userId,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            RedirectAttributes redirectAttributes) {

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

        StringBuilder redirectUrl = new StringBuilder("/admin/users");
        boolean hasParam = false;

        if (username != null && !username.isEmpty()) {
            redirectUrl.append(hasParam ? "&" : "?").append("username=").append(username);
            hasParam = true;
        }

        if (email != null && !email.isEmpty()) {
            redirectUrl.append(hasParam ? "&" : "?").append("email=").append(email);
            hasParam = true;
        }

        redirectUrl.append(hasParam ? "&" : "?").append("page=").append(page);
        hasParam = true;

        redirectUrl.append(hasParam ? "&" : "?").append("size=").append(size);

        return "redirect:" + redirectUrl.toString();
    }
}