package com.connectify.controller;

import com.connectify.entity.Event;
import com.connectify.entity.EventStatus;
import com.connectify.service.EventService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
        List<Event> pendingEvents = filter(events, EventStatus.PENDING_REVIEW);
        List<Event> observedEvents = filter(events, EventStatus.OBSERVED);
        List<Event> rejectedEvents = filter(events, EventStatus.REJECTED);
        List<Event> approvedEvents = events.stream()
                .filter(event -> event.getStatus() == EventStatus.APPROVED || event.getStatus() == EventStatus.PUBLISHED)
                .toList();

        model.addAttribute("events", events);
        model.addAttribute("pendingEvents", pendingEvents);
        model.addAttribute("observedEvents", observedEvents);
        model.addAttribute("rejectedEvents", rejectedEvents);
        model.addAttribute("approvedEvents", approvedEvents);
        model.addAttribute("pendingCount", pendingEvents.size());
        model.addAttribute("approvedCount", approvedEvents.size());
        model.addAttribute("rejectedCount", rejectedEvents.size());
        return "dashboard/admin/events";
    }

    private List<Event> filter(List<Event> events, EventStatus status) {
        return events.stream().filter(event -> event.getStatus() == status).toList();
    }
}
