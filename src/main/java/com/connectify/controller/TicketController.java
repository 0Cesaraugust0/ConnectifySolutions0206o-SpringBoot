package com.connectify.controller;

import com.connectify.entity.Ticket;
import com.connectify.service.EventService;
import com.connectify.service.TicketService;
import com.connectify.service.TicketService.GateValidationResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final EventService eventService;

    public TicketController(TicketService ticketService, EventService eventService) {
        this.ticketService = ticketService;
        this.eventService = eventService;
    }

    @GetMapping("/validate")
    public String validateForm(@RequestParam(required = false) String code, Model model) {
        if (code != null && !code.isBlank()) {
            Optional<Ticket> ticket = ticketService.findByCode(code);
            model.addAttribute("searched", true);
            model.addAttribute("ticket", ticket.orElse(null));
            model.addAttribute("code", code);
        }
        return "tickets/validate";
    }

    @PostMapping("/validate")
    public String validateTicket(@RequestParam String code, Model model) {
        Optional<Ticket> ticket = ticketService.validateTicket(code);
        model.addAttribute("searched", true);
        model.addAttribute("validated", ticket.isPresent());
        model.addAttribute("ticket", ticket.orElse(null));
        model.addAttribute("code", code);
        return "tickets/validate";
    }

    @GetMapping("/gate")
    public String gate(@RequestParam(required = false) Long eventId, Model model) {
        model.addAttribute("events", eventService.findAll());
        model.addAttribute("eventId", eventId);
        if (eventId != null) {
            eventService.findById(eventId).ifPresent(event -> model.addAttribute("selectedEvent", event));
        }
        return "tickets/gate";
    }

    @PostMapping("/gate")
    public String validateAtGate(@RequestParam Long eventId,
                                 @RequestParam String code,
                                 Model model) {
        GateValidationResult result = ticketService.validateTicketForEvent(code, eventId);
        model.addAttribute("events", eventService.findAll());
        model.addAttribute("eventId", eventId);
        model.addAttribute("code", code);
        model.addAttribute("result", result);
        eventService.findById(eventId).ifPresent(event -> model.addAttribute("selectedEvent", event));
        return "tickets/gate";
    }
}
