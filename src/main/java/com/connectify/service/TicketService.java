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

    @Transactional
    public GateValidationResult validateTicketForEvent(String code, Long eventId) {
        Optional<Ticket> ticketOptional = findByCode(code);

        if (ticketOptional.isEmpty()) {
            return GateValidationResult.notFound(code);
        }

        Ticket ticket = ticketOptional.get();

        if (ticket.getEvent() == null || !ticket.getEvent().getId().equals(eventId)) {
            return GateValidationResult.wrongEvent(ticket);
        }

        if (ticket.isUsed()) {
            return GateValidationResult.alreadyUsed(ticket);
        }

        ticket.setUsed(true);
        ticketRepository.save(ticket);
        return GateValidationResult.valid(ticket);
    }

    public static class GateValidationResult {
        private final String status;
        private final String message;
        private final Ticket ticket;
        private final String code;

        private GateValidationResult(String status, String message, Ticket ticket, String code) {
            this.status = status;
            this.message = message;
            this.ticket = ticket;
            this.code = code;
        }

        public static GateValidationResult valid(Ticket ticket) {
            return new GateValidationResult("VALID", "Ticket válido. Acceso autorizado.", ticket, ticket.getCode());
        }

        public static GateValidationResult alreadyUsed(Ticket ticket) {
            return new GateValidationResult("USED", "Ticket encontrado, pero ya fue usado anteriormente.", ticket, ticket.getCode());
        }

        public static GateValidationResult wrongEvent(Ticket ticket) {
            return new GateValidationResult("WRONG_EVENT", "Ticket válido, pero pertenece a otro evento.", ticket, ticket.getCode());
        }

        public static GateValidationResult notFound(String code) {
            return new GateValidationResult("NOT_FOUND", "Ticket no encontrado. Código inválido.", null, code);
        }

        public String getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public Ticket getTicket() {
            return ticket;
        }

        public String getCode() {
            return code;
        }
    }
}
