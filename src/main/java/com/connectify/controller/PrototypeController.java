package com.connectify.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PrototypeController {

    @GetMapping("/prototype")
    public String prototype() {
        return "prototype/index";
    }

    @GetMapping("/prototype/{role}")
    public String rolePrototype(@PathVariable String role, Model model) {
        String normalizedRole = role == null ? "admin" : role.toLowerCase();
        model.addAttribute("role", normalizedRole);
        model.addAttribute("title", titleFor(normalizedRole));
        model.addAttribute("description", descriptionFor(normalizedRole));
        return "prototype/role";
    }

    private String titleFor(String role) {
        return switch (role) {
            case "organizer" -> "Panel prototipo del Organizador";
            case "designer" -> "Panel prototipo del Diseñador";
            case "client" -> "Panel prototipo del Cliente";
            case "gate" -> "Panel prototipo de Control de Puerta";
            default -> "Panel prototipo del Administrador";
        };
    }

    private String descriptionFor(String role) {
        return switch (role) {
            case "organizer" -> "Coordina eventos, solicitudes de diseño, ventas, tickets y comunicación con administración.";
            case "designer" -> "Gestiona solicitudes visuales, portadas, banners, observaciones y entregables de diseño.";
            case "client" -> "Explora eventos, revisa tickets, solicita soporte y consulta comunicaciones vinculadas.";
            case "gate" -> "Opera el ingreso al evento mediante código del evento, contraseña del día y validación de ticket.";
            default -> "Supervisa eventos, accesos de puerta, comunicaciones, soporte, revisión y control general.";
        };
    }
}
