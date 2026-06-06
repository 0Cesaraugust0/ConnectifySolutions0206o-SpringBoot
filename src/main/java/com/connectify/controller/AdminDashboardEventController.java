package com.connectify.controller;

import com.connectify.entity.Event;
import com.connectify.entity.EventStatus;
import com.connectify.service.EventService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/dashboard/admin/events")
public class AdminDashboardEventController {

    private final EventService eventService;

    public AdminDashboardEventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public String board(Model model) {
        List<Event> events = eventService.findAll();
        model.addAttribute("events", events);
        model.addAttribute("pendingEvents", filter(events, EventStatus.PENDING_REVIEW));
        model.addAttribute("observedEvents", events.stream()
                .filter(event -> event.getStatus() == EventStatus.OBSERVED || event.getStatus() == EventStatus.REJECTED)
                .toList());
        model.addAttribute("approvedEvents", events.stream()
                .filter(event -> event.getStatus() == EventStatus.APPROVED || event.getStatus() == EventStatus.PUBLISHED)
                .toList());
        return "dashboard/admin/events";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam EventStatus status) {
        Event event = eventService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
        event.setStatus(status);
        eventService.save(event);
        return "redirect:/dashboard/admin/events";
    }

    private List<Event> filter(List<Event> events, EventStatus status) {
        return events.stream().filter(event -> event.getStatus() == status).toList();
    }
}
