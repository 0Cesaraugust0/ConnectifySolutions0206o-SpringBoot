package com.connectify.controller;

import com.connectify.entity.Event;
import com.connectify.entity.Ticket;
import com.connectify.repository.EventRepository;
import com.connectify.repository.TicketRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequestMapping("/tickets/validate")
public class TicketValidationController {

    private final TicketRepository ticketRepository;
    private final EventRepository eventRepository;

    public TicketValidationController(TicketRepository ticketRepository, EventRepository eventRepository) {
        this.ticketRepository = ticketRepository;
        this.eventRepository = eventRepository;
    }

    @GetMapping
    public String form(Model model) {
        model.addAttribute("gateUnlocked", false);
        return "tickets/validate";
    }

    @PostMapping("/open")
    public String openGate(@RequestParam String eventCode,
                           @RequestParam String gatePassword,
                           Model model) {
        Optional<Event> event = eventRepository.findByGateAccessCode(eventCode.trim());
        if (event.isEmpty() || event.get().getGatePassword() == null || !event.get().getGatePassword().equals(gatePassword)) {
            model.addAttribute("gateError", "Código de evento o contraseña incorrectos.");
            model.addAttribute("gateUnlocked", false);
            model.addAttribute("eventCode", eventCode);
            return "tickets/validate";
        }

        model.addAttribute("gateUnlocked", true);
        model.addAttribute("selectedEvent", event.get());
        model.addAttribute("eventCode", eventCode);
        model.addAttribute("gatePassword", gatePassword);
        return "tickets/validate";
    }

    @PostMapping("/consult")
    public String consult(@RequestParam Long eventId,
                          @RequestParam String eventCode,
                          @RequestParam String gatePassword,
                          @RequestParam String code,
                          Model model) {
        Event selectedEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
        model.addAttribute("gateUnlocked", true);
        model.addAttribute("selectedEvent", selectedEvent);
        model.addAttribute("eventCode", eventCode);
        model.addAttribute("gatePassword", gatePassword);
        model.addAttribute("code", code);
        model.addAttribute("searched", true);

        Optional<Ticket> ticket = ticketRepository.findByCode(code.trim());
        if (ticket.isEmpty()) {
            model.addAttribute("ticket", null);
            return "tickets/validate";
        }

        Ticket found = ticket.get();
        if (!found.getEvent().getId().equals(eventId)) {
            model.addAttribute("ticket", found);
            model.addAttribute("ticketError", "El ticket existe, pero pertenece a otro evento.");
            return "tickets/validate";
        }

        model.addAttribute("ticket", found);
        return "tickets/validate";
    }

    @PostMapping
    public String validateAndUse(@RequestParam Long eventId,
                                 @RequestParam String eventCode,
                                 @RequestParam String gatePassword,
                                 @RequestParam String code,
                                 Model model) {
        Event selectedEvent = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
        model.addAttribute("gateUnlocked", true);
        model.addAttribute("selectedEvent", selectedEvent);
        model.addAttribute("eventCode", eventCode);
        model.addAttribute("gatePassword", gatePassword);
        model.addAttribute("code", code);
        model.addAttribute("searched", true);

        Optional<Ticket> ticket = ticketRepository.findByCode(code.trim());
        if (ticket.isEmpty()) {
            model.addAttribute("result", new ValidationResult("NOT_FOUND", code, "Ticket no encontrado."));
            return "tickets/validate";
        }

        Ticket found = ticket.get();
        model.addAttribute("ticket", found);

        if (!found.getEvent().getId().equals(eventId)) {
            model.addAttribute("result", new ValidationResult("WRONG_EVENT", code, "El ticket pertenece a otro evento."));
            return "tickets/validate";
        }

        if (found.isUsed()) {
            model.addAttribute("result", new ValidationResult("USED", code, "El ticket ya fue usado anteriormente."));
            return "tickets/validate";
        }

        found.setUsed(true);
        ticketRepository.save(found);
        model.addAttribute("result", new ValidationResult("VALID", code, "Acceso autorizado."));
        return "tickets/validate";
    }

    public static class ValidationResult {
        private final String status;
        private final String code;
        private final String message;

        public ValidationResult(String status, String code, String message) {
            this.status = status;
            this.code = code;
            this.message = message;
        }

        public String getStatus() {
            return status;
        }

        public String getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }
}
