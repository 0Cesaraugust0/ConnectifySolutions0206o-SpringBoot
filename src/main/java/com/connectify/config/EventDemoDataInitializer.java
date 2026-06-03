package com.connectify.config;

import com.connectify.entity.Category;
import com.connectify.entity.Event;
import com.connectify.entity.EventStatus;
import com.connectify.repository.CategoryRepository;
import com.connectify.repository.EventRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class EventDemoDataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    public EventDemoDataInitializer(CategoryRepository categoryRepository, EventRepository eventRepository) {
        this.categoryRepository = categoryRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    public void run(String... args) {
        if (eventRepository.count() > 0) {
            return;
        }

        Category music = categoryRepository.findByName("Música")
                .orElseGet(() -> categoryRepository.save(new Category("Música", "🎵", "Conciertos y festivales musicales")));
        Category tech = categoryRepository.findByName("Tecnología")
                .orElseGet(() -> categoryRepository.save(new Category("Tecnología", "💻", "Charlas, conferencias y networking tech")));
        Category food = categoryRepository.findByName("Gastronomía")
                .orElseGet(() -> categoryRepository.save(new Category("Gastronomía", "🍽️", "Ferias, catas y experiencias culinarias")));
        Category sports = categoryRepository.findByName("Deporte")
                .orElseGet(() -> categoryRepository.save(new Category("Deporte", "⚽", "Eventos deportivos y competencias")));

        eventRepository.save(new Event(
                "Concierto Rock Lima 2026",
                "Una noche con bandas nacionales, luces y experiencia premium para fans del rock.",
                music,
                LocalDateTime.now().plusDays(14).withHour(20).withMinute(0),
                "Arena Costa Verde",
                "Lima",
                new BigDecimal("85.00"),
                1200,
                340,
                "",
                true,
                EventStatus.PUBLISHED
        ));

        eventRepository.save(new Event(
                "Tech Summit Perú",
                "Conferencia para estudiantes, desarrolladores y empresas sobre IA, cloud y desarrollo web.",
                tech,
                LocalDateTime.now().plusDays(21).withHour(9).withMinute(30),
                "Centro de Convenciones de Lima",
                "Lima",
                new BigDecimal("120.00"),
                800,
                210,
                "",
                true,
                EventStatus.PUBLISHED
        ));

        eventRepository.save(new Event(
                "Festival Gastronómico Sabores del Perú",
                "Stands, chefs invitados, música en vivo y degustaciones de comida peruana.",
                food,
                LocalDateTime.now().plusDays(10).withHour(12).withMinute(0),
                "Parque de la Exposición",
                "Lima",
                new BigDecimal("35.00"),
                2000,
                870,
                "",
                false,
                EventStatus.PUBLISHED
        ));

        eventRepository.save(new Event(
                "Final Copa Interbarrios",
                "Evento deportivo local con tribunas, zona familiar y validación de tickets QR.",
                sports,
                LocalDateTime.now().plusDays(7).withHour(16).withMinute(0),
                "Estadio Municipal",
                "Callao",
                new BigDecimal("25.00"),
                1500,
                920,
                "",
                false,
                EventStatus.PUBLISHED
        ));
    }
}
