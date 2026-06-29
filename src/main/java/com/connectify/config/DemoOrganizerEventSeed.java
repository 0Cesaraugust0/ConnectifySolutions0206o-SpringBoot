package com.connectify.config;

import com.connectify.entity.Category;
import com.connectify.entity.Event;
import com.connectify.entity.EventStatus;
import com.connectify.repository.CategoryRepository;
import com.connectify.repository.EventRepository;
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
    CommandLineRunner demoOrganizerEvent(EventRepository events, CategoryRepository categories) {
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
            events.save(event);
        };
    }
}
