package com.connectify.service;

import com.connectify.entity.Ticket;
import com.connectify.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;

    public TicketService(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }

    public Optional<Ticket> findByCode(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        return ticketRepository.findByCode(code.trim());
    }

    @Transactional
    public Optional<Ticket> validateTicket(String code) {
        Optional<Ticket> ticketOptional = findByCode(code);
        ticketOptional.ifPresent(ticket -> {
            if (!ticket.isUsed()) {
                ticket.setUsed(true);
                ticketRepository.save(ticket);
            }
        });
        return ticketOptional;
    }
}
