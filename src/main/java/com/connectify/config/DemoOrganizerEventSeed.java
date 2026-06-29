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
            if (events.findAll().stream().anyMatch(event -> "Noches del Valle: Música y Sabores".equals(event.getTitle()))) return;
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
            Event saved = events.save(event);
            ticket(tickets, saved, "General", "Acceso general y feria gastronómica.", "45.00", 360);
            ticket(tickets, saved, "VIP", "Zona preferente y degustación especial.", "95.00", 90);
        };
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
