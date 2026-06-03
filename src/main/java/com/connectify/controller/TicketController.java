package com.connectify.controller;

import com.connectify.entity.Ticket;
import com.connectify.service.TicketService;
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

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
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
}
