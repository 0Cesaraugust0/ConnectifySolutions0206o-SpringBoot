package com.connectify.controller;

import com.connectify.entity.Event;
import com.connectify.service.EventService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) String city,
                       @RequestParam(required = false) String category,
                       Model model) {
        model.addAttribute("events", eventService.findPublished(q, city, category));
        model.addAttribute("q", q);
        model.addAttribute("city", city);
        model.addAttribute("category", category);
        return "events/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Event event = eventService.findById(id).orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
        model.addAttribute("event", event);
        return "events/detail";
    }
}
