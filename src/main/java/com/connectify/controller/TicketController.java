package com.connectify.controller;

import com.connectify.entity.Event;
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
    public String gate(Model model) {
        model.addAttribute("events", eventService.findAll());
        return "tickets/gate";
    }

    @PostMapping("/gate/open")
    public String openGate(@RequestParam String eventCode,
                           @RequestParam String gatePassword,
                           Model model) {
        model.addAttribute("events", eventService.findAll());
        model.addAttribute("eventCode", eventCode);
        model.addAttribute("gatePassword", gatePassword);

        Optional<Event> eventOptional = eventService.findAll().stream()
                .filter(event -> eventCode.equalsIgnoreCase(event.getGateAccessCode()))
                .findFirst();

        if (eventOptional.isEmpty()) {
            model.addAttribute("gateError", "Código de evento no encontrado.");
            return "tickets/gate";
        }

        Event event = eventOptional.get();
        if (event.getGatePassword() == null || !event.getGatePassword().equals(gatePassword)) {
            model.addAttribute("gateError", "Contraseña del día incorrecta.");
            return "tickets/gate";
        }

        model.addAttribute("selectedEvent", event);
        return "tickets/gate";
    }

    @PostMapping("/gate")
    public String validateAtGate(@RequestParam Long eventId,
                                 @RequestParam String eventCode,
                                 @RequestParam String gatePassword,
                                 @RequestParam String code,
                                 Model model) {
        model.addAttribute("events", eventService.findAll());
        model.addAttribute("eventCode", eventCode);
        model.addAttribute("gatePassword", gatePassword);
        model.addAttribute("code", code);

        Optional<Event> eventOptional = eventService.findById(eventId);
        if (eventOptional.isEmpty()) {
            model.addAttribute("gateError", "Evento no encontrado.");
            return "tickets/gate";
        }

        Event event = eventOptional.get();
        if (!eventCode.equalsIgnoreCase(event.getGateAccessCode()) || event.getGatePassword() == null || !event.getGatePassword().equals(gatePassword)) {
            model.addAttribute("gateError", "Acceso de puerta no autorizado.");
            return "tickets/gate";
        }

        GateValidationResult result = ticketService.validateTicketForEvent(code, eventId);
        model.addAttribute("selectedEvent", event);
        model.addAttribute("result", result);
        return "tickets/gate";
    }
}
