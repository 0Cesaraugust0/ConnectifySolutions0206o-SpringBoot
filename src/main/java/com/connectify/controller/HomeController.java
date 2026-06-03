package com.connectify.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("projectName", "Connectify Solutions");
        model.addAttribute("subtitle", "Migración progresiva a Spring Boot + Thymeleaf + JPA + MySQL/MariaDB");
        return "index";
    }
}
