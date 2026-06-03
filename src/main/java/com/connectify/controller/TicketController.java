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
    public String validateForm(Model model) {
        return "tickets/validate";
    }

    @PostMapping("/validate/open")
    public String openValidation(@RequestParam String eventCode,
                                 @RequestParam String gatePassword,
                                 Model model) {
        model.addAttribute("eventCode", eventCode);
        model.addAttribute("gatePassword", gatePassword);

        Optional<Event> eventOptional = findEventByGateCode(eventCode);
        if (eventOptional.isEmpty()) {
            model.addAttribute("gateError", "Código de evento no encontrado.");
            return "tickets/validate";
        }

        Event event = eventOptional.get();
        if (!isGatePasswordValid(event, gatePassword)) {
            model.addAttribute("gateError", "Contraseña del día incorrecta.");
            return "tickets/validate";
        }

        model.addAttribute("selectedEvent", event);
        model.addAttribute("gateUnlocked", true);
        return "tickets/validate";
    }

    @PostMapping("/validate/consult")
    public String consultTicket(@RequestParam Long eventId,
                                @RequestParam String eventCode,
                                @RequestParam String gatePassword,
                                @RequestParam String code,
                                Model model) {
        model.addAttribute("eventCode", eventCode);
        model.addAttribute("gatePassword", gatePassword);
        model.addAttribute("code", code);
        model.addAttribute("searched", true);

        Optional<Event> eventOptional = authorizeGate(eventId, eventCode, gatePassword, model);
        if (eventOptional.isEmpty()) {
            return "tickets/validate";
        }

        Optional<Ticket> ticket = ticketService.findByCode(code);
        model.addAttribute("selectedEvent", eventOptional.get());
        model.addAttribute("gateUnlocked", true);
        model.addAttribute("ticket", ticket.orElse(null));

        if (ticket.isPresent() && ticket.get().getEvent() != null && !ticket.get().getEvent().getId().equals(eventId)) {
            model.addAttribute("ticketError", "El ticket existe, pero pertenece a otro evento.");
        }

        return "tickets/validate";
    }

    @PostMapping("/validate")
    public String validateTicket(@RequestParam Long eventId,
                                 @RequestParam String eventCode,
                                 @RequestParam String gatePassword,
                                 @RequestParam String code,
                                 Model model) {
        model.addAttribute("eventCode", eventCode);
        model.addAttribute("gatePassword", gatePassword);
        model.addAttribute("code", code);
        model.addAttribute("searched", true);

        Optional<Event> eventOptional = authorizeGate(eventId, eventCode, gatePassword, model);
        if (eventOptional.isEmpty()) {
            return "tickets/validate";
        }

        GateValidationResult result = ticketService.validateTicketForEvent(code, eventId);
        model.addAttribute("selectedEvent", eventOptional.get());
        model.addAttribute("gateUnlocked", true);
        model.addAttribute("result", result);
        model.addAttribute("ticket", result.getTicket());
        return "tickets/validate";
    }

    @GetMapping("/gate")
    public String gateRedirect() {
        return "redirect:/tickets/validate";
    }

    private Optional<Event> authorizeGate(Long eventId, String eventCode, String gatePassword, Model model) {
        Optional<Event> eventOptional = eventService.findById(eventId);
        if (eventOptional.isEmpty()) {
            model.addAttribute("gateError", "Evento no encontrado.");
            return Optional.empty();
        }

        Event event = eventOptional.get();
        if (!eventCode.equalsIgnoreCase(event.getGateAccessCode()) || !isGatePasswordValid(event, gatePassword)) {
            model.addAttribute("gateError", "Acceso de validación no autorizado.");
            return Optional.empty();
        }

        return eventOptional;
    }

    private Optional<Event> findEventByGateCode(String eventCode) {
        return eventService.findAll().stream()
                .filter(event -> event.getGateAccessCode() != null && event.getGateAccessCode().equalsIgnoreCase(eventCode))
                .findFirst();
    }

    private boolean isGatePasswordValid(Event event, String gatePassword) {
        return event.getGatePassword() != null && event.getGatePassword().equals(gatePassword);
    }
}
