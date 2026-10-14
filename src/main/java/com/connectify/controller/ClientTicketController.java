package com.connectify.controller;

import com.connectify.entity.Ticket;
import com.connectify.repository.TicketRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

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
        List<Ticket> tickets = ticketRepository.findByAttendeeEmailOrderByGeneratedAtDesc(email);
        long usedTickets = tickets.stream().filter(Ticket::isUsed).count();
        model.addAttribute("tickets", tickets);
        model.addAttribute("ticketCount", tickets.size());
        model.addAttribute("availableTicketCount", tickets.size() - usedTickets);
        model.addAttribute("usedTicketCount", usedTickets);
        model.addAttribute("email", email);
        return "dashboard/client/tickets";
    }
}
