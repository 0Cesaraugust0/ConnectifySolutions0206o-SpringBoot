package com.connectify.controller;

import com.connectify.entity.Event;
import com.connectify.entity.EventStatus;
import com.connectify.entity.TicketType;
import com.connectify.repository.TicketTypeRepository;
import com.connectify.service.CartService;
import com.connectify.service.EventService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/cart")
public class MarketplaceCartShortcutController {

    private final EventService eventService;
    private final TicketTypeRepository ticketTypes;
    private final CartService cartService;

    public MarketplaceCartShortcutController(EventService eventService,
                                             TicketTypeRepository ticketTypes,
                                             CartService cartService) {
        this.eventService = eventService;
        this.ticketTypes = ticketTypes;
        this.cartService = cartService;
    }

    @GetMapping("/add/{eventId}")
    public String addOne(@PathVariable Long eventId,
                         @RequestParam Long ticketTypeId,
                         HttpSession session) {
        Event event = eventService.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
        if (event.getStatus() != EventStatus.PUBLISHED) {
            return "redirect:/events";
        }
        TicketType type = ticketTypes.findById(ticketTypeId)
                .orElseThrow(() -> new IllegalArgumentException("Tipo de entrada no encontrado"));
        if (!type.isActive() || type.getEvent() == null || !eventId.equals(type.getEvent().getId())) {
            return "redirect:/events/" + eventId;
        }
        cartService.addEvent(session, event, type, 1);
        return "redirect:/cart";
    }
}
