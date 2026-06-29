package com.connectify.controller;

import com.connectify.entity.Category;
import com.connectify.entity.Event;
import com.connectify.entity.EventAdminRecord;
import com.connectify.entity.EventAdminRecordType;
import com.connectify.entity.EventStatus;
import com.connectify.entity.MessagePriority;
import com.connectify.entity.MessageType;
import com.connectify.entity.Role;
import com.connectify.entity.TicketType;
import com.connectify.repository.CategoryRepository;
import com.connectify.repository.EventAdminRecordRepository;
import com.connectify.repository.EventRepository;
import com.connectify.repository.TicketTypeRepository;
import com.connectify.service.InternalMessageService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/dashboard/organizer/events")
public class OrganizerEventCreationController {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final EventAdminRecordRepository recordRepository;
    private final InternalMessageService messageService;

    public OrganizerEventCreationController(EventRepository eventRepository,
                                            CategoryRepository categoryRepository,
                                            TicketTypeRepository ticketTypeRepository,
                                            EventAdminRecordRepository recordRepository,
                                            InternalMessageService messageService) {
        this.eventRepository = eventRepository;
        this.categoryRepository = categoryRepository;
        this.ticketTypeRepository = ticketTypeRepository;
        this.recordRepository = recordRepository;
        this.messageService = messageService;
    }

    @PostMapping("/create-with-tickets")
    public String createWithTickets(@RequestParam String title,
                                    @RequestParam String description,
                                    @RequestParam Long categoryId,
                                    @RequestParam String eventDate,
                                    @RequestParam String location,
                                    @RequestParam String city,
                                    @RequestParam BigDecimal price,
                                    @RequestParam Integer capacity,
                                    @RequestParam(required = false) String imageUrl,
                                    @RequestParam(required = false) List<String> ticketName,
                                    @RequestParam(required = false) List<BigDecimal> ticketPrice,
                                    @RequestParam(required = false) List<Integer> ticketQuantity,
                                    @RequestParam(required = false) List<String> ticketDescription,
                                    @RequestParam(required = false, defaultValue = "false") boolean requestDesigner,
                                    @RequestParam(required = false) String designBrief,
                                    Authentication authentication) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));

        String organizerEmail = currentEmail(authentication);
        Event event = new Event();
        event.setTitle(title.trim());
        event.setDescription(description.trim());
        event.setCategory(category);
        event.setEventDate(LocalDateTime.parse(eventDate));
        event.setLocation(location.trim());
        event.setCity(city.trim());
        event.setOrganizerName(displayNameFromEmail(organizerEmail));
        event.setOrganizerEmail(organizerEmail);
        event.setPrice(price);
        event.setCapacity(capacity);
        event.setSold(0);
        event.setImageUrl(imageUrl == null ? "" : imageUrl.trim());
        event.setFeatured(false);
        event.setDesignEnabled(requestDesigner);
        event.setStatus(EventStatus.PENDING_REVIEW);
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());

        Event saved = eventRepository.save(event);
        createRecord(saved, EventAdminRecordType.CREATED,
                "Evento creado por organizador con información principal y enviado a revisión administrativa.");

        TicketSummary ticketSummary = createTicketTypes(saved, ticketName, ticketPrice, ticketQuantity, ticketDescription);
        if (ticketSummary.createdCount() > 0) {
            if (ticketSummary.lowestPrice() != null) {
                saved.setPrice(ticketSummary.lowestPrice());
            }
            if (ticketSummary.totalQuantity() > saved.getCapacity()) {
                saved.setCapacity(ticketSummary.totalQuantity());
            }
            eventRepository.save(saved);
            createRecord(saved, EventAdminRecordType.TICKET_TYPE_CREATED,
                    "Tipos de entrada creados durante la construcción inicial:\n\n" + ticketSummary.description());
        }

        if (requestDesigner) {
            String brief = hasText(designBrief)
                    ? designBrief.trim()
                    : "Solicito una propuesta visual para presentar este evento en el marketplace.";
            messageService.create("Organizador", organizerEmail, Role.ORGANIZER, Role.DESIGNER,
                    MessageType.DESIGN_REQUEST, MessagePriority.NORMAL,
                    "Solicitud visual: " + saved.getTitle(), brief, saved.getId());
            createRecord(saved, EventAdminRecordType.ADMIN_COPY,
                    "Solicitud visual enviada al Diseñador junto con la creación del evento.");
        }

        return "redirect:/dashboard/organizer/events/" + saved.getId() + "?created=true";
    }

    private TicketSummary createTicketTypes(Event event, List<String> names, List<BigDecimal> prices,
                                            List<Integer> quantities, List<String> descriptions) {
        if (names == null || prices == null || quantities == null) {
            return new TicketSummary(0, 0, null, "");
        }

        int rows = Math.min(names.size(), Math.min(prices.size(), quantities.size()));
        int created = 0;
        int totalQuantity = 0;
        BigDecimal lowestPrice = null;
        StringBuilder description = new StringBuilder();

        for (int index = 0; index < rows; index++) {
            String name = names.get(index);
            BigDecimal ticketPrice = prices.get(index);
            Integer quantity = quantities.get(index);
            if (!hasText(name) || ticketPrice == null || quantity == null || quantity < 1 || ticketPrice.signum() < 0) {
                continue;
            }

            String ticketDescription = descriptions != null && index < descriptions.size() && hasText(descriptions.get(index))
                    ? descriptions.get(index).trim()
                    : "Entrada " + name.trim() + " para " + event.getTitle();

            TicketType ticket = new TicketType();
            ticket.setEvent(event);
            ticket.setName(name.trim());
            ticket.setDescription(ticketDescription);
            ticket.setPrice(ticketPrice);
            ticket.setQuantityAvailable(quantity);
            ticket.setQuantitySold(0);
            ticket.setActive(true);
            ticketTypeRepository.save(ticket);

            created++;
            totalQuantity += quantity;
            lowestPrice = lowestPrice == null || ticketPrice.compareTo(lowestPrice) < 0 ? ticketPrice : lowestPrice;
            if (description.length() > 0) {
                description.append("\n");
            }
            description.append("• ").append(name.trim()).append(" | S/ ").append(ticketPrice)
                    .append(" | Cupos: ").append(quantity);
        }

        return new TicketSummary(created, totalQuantity, lowestPrice, description.toString());
    }

    private String currentEmail(Authentication authentication) {
        if (authentication == null || !hasText(authentication.getName())) {
            throw new IllegalStateException("Se requiere una sesión de organizador");
        }
        return authentication.getName();
    }

    private String displayNameFromEmail(String email) {
        String localPart = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
        String normalized = localPart.replace('.', ' ').replace('_', ' ').replace('-', ' ').trim();
        return normalized.isBlank() ? "Organizador" : normalized.substring(0, 1).toUpperCase() + normalized.substring(1);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private void createRecord(Event event, EventAdminRecordType type, String description) {
        EventAdminRecord record = new EventAdminRecord();
        record.setEvent(event);
        record.setType(type);
        record.setDescription(description);
        record.setCreatedAt(LocalDateTime.now());
        recordRepository.save(record);
    }

    private record TicketSummary(int createdCount, int totalQuantity, BigDecimal lowestPrice, String description) {
    }
}
