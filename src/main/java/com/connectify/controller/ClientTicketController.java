package com.connectify.controller;

import com.connectify.repository.TicketRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard/client/tickets")
public class ClientTicketController {

    private final TicketRepository ticketRepository;

    public ClientTicketController(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @GetMapping
    public String tickets(Model model, Authentication authentication) {
        String email = authentication != null ? authentication.getName() : "";
        model.addAttribute("tickets", ticketRepository.findByAttendeeEmailOrderByGeneratedAtDesc(email));
        model.addAttribute("email", email);
        return "dashboard/client/tickets";
    }
}
