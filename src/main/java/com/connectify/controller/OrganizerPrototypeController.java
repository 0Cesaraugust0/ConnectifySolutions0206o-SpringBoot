package com.connectify.controller;

import com.connectify.entity.Category;
import com.connectify.entity.Event;
import com.connectify.entity.EventAdminRecord;
import com.connectify.entity.EventAdminRecordType;
import com.connectify.entity.EventStatus;
import com.connectify.entity.TicketType;
import com.connectify.repository.CategoryRepository;
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

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/prototype/organizer/events")
public class OrganizerPrototypeController {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final EventAdminRecordRepository recordRepository;

    public OrganizerPrototypeController(EventRepository eventRepository,
                                        CategoryRepository categoryRepository,
                                        TicketTypeRepository ticketTypeRepository,
                                        EventAdminRecordRepository recordRepository) {
        this.eventRepository = eventRepository;
        this.categoryRepository = categoryRepository;
        this.ticketTypeRepository = ticketTypeRepository;
        this.recordRepository = recordRepository;
    }

    @GetMapping
    public String myEvents(Model model) {
        model.addAttribute("events", eventRepository.findAll());
        return "prototype/organizer/events";
    }

    @GetMapping("/new")
    public String newEvent(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        return "prototype/organizer/event-form";
    }

    @GetMapping("/{id}/ticket-types")
    public String ticketTypes(@PathVariable Long id, Model model) {
        Event event = findEvent(id);
        model.addAttribute("event", event);
        model.addAttribute("ticketTypes", ticketTypeRepository.findByEventIdOrderByPriceAsc(id));
        model.addAttribute("records", recordRepository.findByEventIdOrderByCreatedAtDesc(id));
        return "prototype/organizer/ticket-types";
    }

    @PostMapping("/{id}/ticket-types")
    public String createTicketType(@PathVariable Long id,
                                   @RequestParam String name,
                                   @RequestParam String description,
                                   @RequestParam BigDecimal price,
                                   @RequestParam Integer quantityAvailable) {
        Event event = findEvent(id);
        TicketType ticketType = new TicketType();
        ticketType.setEvent(event);
        ticketType.setName(name);
        ticketType.setDescription(description);
        ticketType.setPrice(price);
        ticketType.setQuantityAvailable(quantityAvailable);
        ticketType.setQuantitySold(0);
        ticketType.setActive(true);
        ticketTypeRepository.save(ticketType);

        createRecord(event, EventAdminRecordType.TICKET_TYPE_CREATED,
                "El organizador creó el tipo de entrada: " + name + " / S/ " + price + " / cantidad " + quantityAvailable);

        return "redirect:/prototype/organizer/events/" + id + "/ticket-types";
    }

    @PostMapping("/{id}/cancel")
    public String cancelEvent(@PathVariable Long id, Model model) {
        Event event = findEvent(id);
        if (!canOrganizerCancel(event)) {
            return "redirect:/prototype/organizer/events?cancelDenied=true";
        }

        event.setStatus(EventStatus.CANCELLED);
        eventRepository.save(event);
        createRecord(event, EventAdminRecordType.CANCELLED_BY_ORGANIZER,
                "Evento cancelado por organizador. Se conserva copia administrativa para revisión legal y trazabilidad.");

        return "redirect:/prototype/organizer/events?cancelled=true";
    }

    @GetMapping("/metrics")
    public String metrics(Model model) {
        List<Event> events = eventRepository.findAll();
        int totalEvents = events.size();
        int totalCapacity = events.stream().map(Event::getCapacity).filter(value -> value != null).mapToInt(Integer::intValue).sum();
        int totalSold = events.stream().map(Event::getSold).filter(value -> value != null).mapToInt(Integer::intValue).sum();
        int availableTickets = Math.max(totalCapacity - totalSold, 0);
        double occupancyRate = totalCapacity == 0 ? 0 : totalSold * 100.0 / totalCapacity;
        BigDecimal revenue = events.stream()
                .map(event -> safePrice(event).multiply(BigDecimal.valueOf(event.getSold() == null ? 0 : event.getSold())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("events", events);
        model.addAttribute("totalEvents", totalEvents);
        model.addAttribute("totalCapacity", totalCapacity);
        model.addAttribute("totalSold", totalSold);
        model.addAttribute("availableTickets", availableTickets);
        model.addAttribute("occupancyRate", String.format("%.1f", occupancyRate));
        model.addAttribute("revenue", revenue);
        return "prototype/organizer/metrics";
    }

    @PostMapping
    public String createEvent(@RequestParam String title,
                              @RequestParam String description,
                              @RequestParam Long categoryId,
                              @RequestParam String eventDate,
                              @RequestParam String location,
                              @RequestParam String city,
                              @RequestParam BigDecimal price,
                              @RequestParam Integer capacity,
                              @RequestParam(required = false) String imageUrl) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));

        Event event = new Event();
        event.setTitle(title);
        event.setDescription(description);
        event.setCategory(category);
        event.setEventDate(LocalDateTime.parse(eventDate));
        event.setLocation(location);
        event.setCity(city);
        event.setPrice(price);
        event.setCapacity(capacity);
        event.setSold(0);
        event.setImageUrl(imageUrl == null ? "" : imageUrl);
        event.setFeatured(false);
        event.setStatus(EventStatus.PUBLISHED);
        Event saved = eventRepository.save(event);

        createRecord(saved, EventAdminRecordType.CREATED,
                "Evento creado por organizador en prototipo. Pendiente de asociación a usuario autenticado en fase login.");

        return "redirect:/prototype/organizer/events";
    }

    private Event findEvent(Long id) {
        return eventRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
    }

    private boolean canOrganizerCancel(Event event) {
        if (event.getEventDate() == null) {
            return true;
        }
        return Duration.between(LocalDateTime.now(), event.getEventDate()).toHours() >= 24;
    }

    private void createRecord(Event event, EventAdminRecordType type, String description) {
        EventAdminRecord record = new EventAdminRecord();
        record.setEvent(event);
        record.setType(type);
        record.setDescription(description);
        recordRepository.save(record);
    }

    private BigDecimal safePrice(Event event) {
        return event.getPrice() == null ? BigDecimal.ZERO : event.getPrice();
    }
}
