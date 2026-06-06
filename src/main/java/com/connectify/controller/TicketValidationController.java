package com.connectify.controller;

import com.connectify.entity.Ticket;
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

    public TicketValidationController(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    @GetMapping
    public String form() {
        return "tickets/validate";
    }

    @PostMapping
    public String validate(@RequestParam String code, Model model) {
        Optional<Ticket> ticket = ticketRepository.findByCode(code.trim());
        if (ticket.isEmpty()) {
            model.addAttribute("error", "No se encontró un ticket con ese código.");
            model.addAttribute("code", code);
            return "tickets/validate";
        }

        model.addAttribute("ticket", ticket.get());
        model.addAttribute("code", code);
        return "tickets/validate";
    }

    @PostMapping("/use")
    public String markAsUsed(@RequestParam String code, Model model) {
        Optional<Ticket> found = ticketRepository.findByCode(code.trim());
        if (found.isEmpty()) {
            model.addAttribute("error", "No se encontró un ticket con ese código.");
            model.addAttribute("code", code);
            return "tickets/validate";
        }

        Ticket ticket = found.get();
        if (ticket.isUsed()) {
            model.addAttribute("warning", "Este ticket ya fue usado anteriormente.");
            model.addAttribute("ticket", ticket);
            return "tickets/validate";
        }

        ticket.setUsed(true);
        ticketRepository.save(ticket);
        model.addAttribute("success", "Ingreso validado correctamente. Ticket marcado como usado.");
        model.addAttribute("ticket", ticket);
        return "tickets/validate";
    }
}
