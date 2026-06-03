package com.connectify.controller;

import com.connectify.entity.Event;
import com.connectify.service.EventService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/events")
public class AdminEventController {

    private final EventService eventService;

    public AdminEventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("events", eventService.findAll());
        return "admin/events";
    }

    @GetMapping("/{id}/gate-access")
    public String gateAccessForm(@PathVariable Long id, Model model) {
        Event event = eventService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
        model.addAttribute("event", event);
        return "admin/gate-access";
    }

    @PostMapping("/{id}/gate-access")
    public String updateGateAccess(@PathVariable Long id,
                                   @RequestParam String gateAccessCode,
                                   @RequestParam String gatePassword) {
        eventService.updateGateAccess(id, gateAccessCode, gatePassword);
        return "redirect:/admin/events";
    }
}
