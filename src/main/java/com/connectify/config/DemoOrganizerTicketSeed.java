package com.connectify.config;

import com.connectify.entity.Event;
import com.connectify.entity.TicketType;
import com.connectify.repository.EventRepository;
import com.connectify.repository.TicketTypeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;

@Configuration
@Profile("!prod")
public class DemoOrganizerTicketSeed {

    @Bean
    CommandLineRunner demoOrganizerTickets(EventRepository events, TicketTypeRepository tickets) {
        return args -> {
            Event event = events.findAll().stream()
                    .filter(item -> "Noches del Valle: Música y Sabores".equals(item.getTitle()))
                    .findFirst().orElse(null);
            if (event == null || !tickets.findByEventIdOrderByPriceAsc(event.getId()).isEmpty()) return;
            save(tickets, event, "General", "Acceso general y feria gastronómica.", "45.00", 360);
            save(tickets, event, "VIP", "Zona preferente y degustación especial.", "95.00", 90);
        };
    }

    private void save(TicketTypeRepository tickets, Event event, String name, String description, String price, int quantity) {
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
