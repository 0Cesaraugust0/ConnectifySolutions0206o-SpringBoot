package com.connectify.controller;

import com.connectify.entity.Event;
import com.connectify.entity.EventAdminRecord;
import com.connectify.entity.EventAdminRecordType;
import com.connectify.entity.EventStatus;
import com.connectify.repository.EventAdminRecordRepository;
import com.connectify.repository.EventRepository;
import com.connectify.repository.TicketTypeRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/prototype/admin/events")
public class AdminPrototypeController {

    private final EventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final EventAdminRecordRepository recordRepository;

    public AdminPrototypeController(EventRepository eventRepository,
                                    TicketTypeRepository ticketTypeRepository,
                                    EventAdminRecordRepository recordRepository) {
        this.eventRepository = eventRepository;
        this.ticketTypeRepository = ticketTypeRepository;
        this.recordRepository = recordRepository;
    }

    @GetMapping
    public String events(Model model) {
        model.addAttribute("events", eventRepository.findAll());
        return "prototype/admin/events";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Event event = findEvent(id);
        model.addAttribute("event", event);
        model.addAttribute("ticketTypes", ticketTypeRepository.findByEventIdOrderByPriceAsc(id));
        model.addAttribute("records", recordRepository.findByEventIdOrderByCreatedAtDesc(id));
        return "prototype/admin/event-detail";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id,
                          @RequestParam(required = false, defaultValue = "Evento aprobado por administración.") String note) {
        Event event = findEvent(id);
        event.setStatus(EventStatus.APPROVED);
        eventRepository.save(event);
        createRecord(event, EventAdminRecordType.ADMIN_COPY, note);
        return "redirect:/prototype/admin/events/" + id + "?approved=true";
    }

    @PostMapping("/{id}/publish")
    public String publish(@PathVariable Long id,
                          @RequestParam(required = false, defaultValue = "Evento publicado por administración.") String note) {
        Event event = findEvent(id);
        event.setStatus(EventStatus.PUBLISHED);
        eventRepository.save(event);
        createRecord(event, EventAdminRecordType.ADMIN_COPY, note);
        return "redirect:/prototype/admin/events/" + id + "?published=true";
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id,
                         @RequestParam(required = false, defaultValue = "Evento rechazado por administración.") String note) {
        Event event = findEvent(id);
        event.setStatus(EventStatus.REJECTED);
        eventRepository.save(event);
        createRecord(event, EventAdminRecordType.ADMIN_COPY, note);
        return "redirect:/prototype/admin/events/" + id + "?rejected=true";
    }

    private Event findEvent(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
    }

    private void createRecord(Event event, EventAdminRecordType type, String description) {
        EventAdminRecord record = new EventAdminRecord();
        record.setEvent(event);
        record.setType(type);
        record.setDescription(description);
        record.setCreatedAt(LocalDateTime.now());
        recordRepository.save(record);
    }
}
