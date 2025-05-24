package com.example.demo.controllers.web;

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

import com.example.demo.dtos.ReviewDTO;
import com.example.demo.dtos.UserDTO;
import com.example.demo.entities.Review;
import com.example.demo.entities.User;
import com.example.demo.services.ReviewService;
import com.example.demo.services.RouteService;
import com.example.demo.services.UserService;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminReviewsController {

    @Autowired
    @Qualifier("userService")
    private UserService userService;

    @Autowired
    @Qualifier("reviewService")
    private ReviewService reviewService;

    @Autowired
    @Qualifier("routeService")
    private RouteService routeService;

    @GetMapping("/reviews")
    public String listReviews(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String date,
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
        Page<ReviewDTO> reviewsPage = reviewService.getFilteredReviewsPaginated(city, date, pageRequest);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
            User currentUser = (User) auth.getPrincipal();
            model.addAttribute("currentUser", new UserDTO(currentUser));
        }

        model.addAttribute("reviews", reviewsPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", reviewsPage.getTotalPages());
        model.addAttribute("totalItems", reviewsPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("cityFilter", city != null ? city : "");
        model.addAttribute("dateFilter", date != null ? date : "");

        return "admin/reviews/list";
    }

    @PostMapping("/deleteReview")
    public String deleteReview(
            @RequestParam Long reviewId,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String date,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            RedirectAttributes redirectAttributes) {

        Review review = reviewService.findById(reviewId);

        if (review != null) {
            boolean deleted = reviewService.deleteReview(reviewId);

            if (deleted) {
                redirectAttributes.addFlashAttribute("message",
                        "Reseña eliminada correctamente.");
            } else {
                redirectAttributes.addFlashAttribute("error", "No se pudo eliminar la reseña.");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Reseña no encontrada.");
        }

        StringBuilder redirectUrl = new StringBuilder("/admin/reviews");
        boolean hasParam = false;

        if (city != null && !city.isEmpty()) {
            redirectUrl.append(hasParam ? "&" : "?").append("city=").append(city);
            hasParam = true;
        }

        if (date != null && !date.isEmpty()) {
            redirectUrl.append(hasParam ? "&" : "?").append("date=").append(date);
            hasParam = true;
        }

        redirectUrl.append(hasParam ? "&" : "?").append("page=").append(page);
        hasParam = true;

        redirectUrl.append(hasParam ? "&" : "?").append("size=").append(size);

        return "redirect:" + redirectUrl.toString();
    }
}