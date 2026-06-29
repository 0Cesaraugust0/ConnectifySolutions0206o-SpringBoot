package com.connectify.config;

import com.connectify.entity.Category;
import com.connectify.entity.Event;
import com.connectify.entity.EventStatus;
import com.connectify.entity.TicketType;
import com.connectify.repository.CategoryRepository;
import com.connectify.repository.EventRepository;
import com.connectify.repository.TicketTypeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
@Profile("!prod")
public class DemoOrganizerEventSeed {

    @Bean
    CommandLineRunner demoOrganizerEvent(EventRepository events, CategoryRepository categories,
                                         TicketTypeRepository tickets) {
        return args -> {
            Event reference = events.findAll().stream()
                    .filter(event -> "Noches del Valle: Música y Sabores".equals(event.getTitle()))
                    .findFirst()
                    .orElseGet(() -> createReferenceEvent(events, categories));
            ensureReferenceTickets(tickets, reference);

            // El evento que aparece en la demostración del Marketplace también debe poder comprarse.
            events.findAll().stream()
                    .filter(event -> event.getStatus() == EventStatus.PUBLISHED)
                    .filter(event -> "Festival Gastronómico Sabores del Perú".equals(event.getTitle()))
                    .forEach(event -> ensureFestivalTickets(tickets, event));
        };
    }

    private Event createReferenceEvent(EventRepository events, CategoryRepository categories) {
        Category music = categories.findByName("Música")
                .orElseGet(() -> categories.save(new Category("Música", "🎵", "Conciertos y experiencias musicales.")));
        Event event = new Event();
        event.setTitle("Noches del Valle: Música y Sabores");
        event.setDescription("Evento de referencia para revisar el flujo de Organizador, Administrador y Cliente.");
        event.setCategory(music);
        event.setOrganizerName("Valeria Torres");
        event.setOrganizerEmail("organizador.demo@connectify.local");
        event.setEventDate(LocalDateTime.now().plusDays(18));
        event.setLocation("Anfiteatro del Parque de la Reserva");
        event.setCity("Lima");
        event.setPrice(new BigDecimal("45.00"));
        event.setCapacity(450);
        event.setSold(38);
        event.setFeatured(true);
        event.setStatus(EventStatus.PUBLISHED);
        event.setCreatedAt(LocalDateTime.now().minusDays(8));
        event.setUpdatedAt(LocalDateTime.now().minusDays(1));
        return events.save(event);
    }

    private void ensureReferenceTickets(TicketTypeRepository tickets, Event event) {
        if (!tickets.findByEventIdOrderByPriceAsc(event.getId()).isEmpty()) return;
        ticket(tickets, event, "General", "Acceso general y feria gastronómica.", "45.00", 360);
        ticket(tickets, event, "VIP", "Zona preferente y degustación especial.", "95.00", 90);
    }

    private void ensureFestivalTickets(TicketTypeRepository tickets, Event event) {
        if (!tickets.findByEventIdOrderByPriceAsc(event.getId()).isEmpty()) return;
        ticket(tickets, event, "General", "Acceso a stands, música en vivo y degustaciones.", "35.00", 1600);
        ticket(tickets, event, "VIP", "Acceso preferente y cata guiada con chefs invitados.", "75.00", 400);
    }

    private void ticket(TicketTypeRepository tickets, Event event, String name, String description, String price, int quantity) {
        TicketType type = new TicketType();
        type.setEvent(event);
        type.setName(name);
        type.setDescription(description);
        type.setPrice(new BigDecimal(price));
        type.setQuantityAvailable(quantity);
        type.setQuantitySold(0);
        type.setActive(true);
        tickets.save(type);
    }
}
