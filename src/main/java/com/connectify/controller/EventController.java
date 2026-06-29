package com.connectify.controller;

import com.connectify.entity.Event;
import com.connectify.entity.EventStatus;
import com.connectify.repository.EventPresentationSettingsRepository;
import com.connectify.repository.TicketTypeRepository;
import com.connectify.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;
    private final TicketTypeRepository ticketTypeRepository;
    private final EventPresentationSettingsRepository presentationSettingsRepository;

    public EventController(EventService eventService,
                           TicketTypeRepository ticketTypeRepository,
                           EventPresentationSettingsRepository presentationSettingsRepository) {
        this.eventService = eventService;
        this.ticketTypeRepository = ticketTypeRepository;
        this.presentationSettingsRepository = presentationSettingsRepository;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) String city,
                       @RequestParam(required = false) String category,
                       Model model) {
        model.addAttribute("events", eventService.findPublished(q, city, category));
        model.addAttribute("q", q);
        model.addAttribute("city", city);
        model.addAttribute("category", category);
        return "events/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Event event = eventService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));
        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no disponible en marketplace");
        }
        model.addAttribute("event", event);
        model.addAttribute("ticketTypes", ticketTypeRepository.findByEventIdAndActiveTrueOrderByPriceAsc(id));
        model.addAttribute("presentation", presentationSettingsRepository.findByEventId(id).orElse(null));
        model.addAttribute("previewMode", false);
        return "events/detail";
    }
}
