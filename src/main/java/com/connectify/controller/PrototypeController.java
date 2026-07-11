package com.connectify.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * La demo pública se limita al flujo esencial del marketplace:
 * crear evento, revisar, comprar entradas y validar QR.
 */
@Controller
public class PrototypeController {

    @GetMapping("/prototype")
    public String prototype() {
        return "prototype/index";
    }

    @GetMapping("/prototype/{role}")
    public String rolePrototype(@PathVariable String role) {
        return "redirect:/prototype";
    }
}
