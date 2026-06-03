package com.connectify.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        boolean developer = hasRole(authentication, "ROLE_DEVELOPER");
        boolean admin = hasRole(authentication, "ROLE_ADMIN");
        boolean organizer = hasRole(authentication, "ROLE_ORGANIZER");
        boolean client = hasRole(authentication, "ROLE_CLIENT");
        boolean designer = hasRole(authentication, "ROLE_DESIGNER");

        if (developer) return "redirect:/dashboard/developer";
        if (admin) return "redirect:/dashboard/admin";
        if (organizer) return "redirect:/dashboard/organizer";
        if (client) return "redirect:/dashboard/client";
        if (designer) return "redirect:/dashboard/designer";

        return "redirect:/";
    }

    @GetMapping("/dashboard/developer")
    public String developer(Model model, Authentication authentication) {
        model.addAttribute("title", "Panel Developer");
        model.addAttribute("email", authentication.getName());
        return "dashboard/developer";
    }

    @GetMapping("/dashboard/admin")
    public String admin(Model model, Authentication authentication) {
        model.addAttribute("title", "Panel Administrador");
        model.addAttribute("email", authentication.getName());
        return "dashboard/admin";
    }

    @GetMapping("/dashboard/organizer")
    public String organizer(Model model, Authentication authentication) {
        model.addAttribute("title", "Panel Organizador");
        model.addAttribute("email", authentication.getName());
        return "dashboard/organizer";
    }

    @GetMapping("/dashboard/client")
    public String client(Model model, Authentication authentication) {
        model.addAttribute("title", "Panel Cliente");
        model.addAttribute("email", authentication.getName());
        return "dashboard/client";
    }

    @GetMapping("/dashboard/designer")
    public String designer(Model model, Authentication authentication) {
        model.addAttribute("title", "Panel Diseñador Web");
        model.addAttribute("email", authentication.getName());
        return "dashboard/designer";
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }
}
