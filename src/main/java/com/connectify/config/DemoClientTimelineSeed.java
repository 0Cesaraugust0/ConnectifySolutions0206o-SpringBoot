package com.connectify.config;

import com.connectify.entity.Event;
import com.connectify.entity.EventAdminRecord;
import com.connectify.entity.EventAdminRecordType;
import com.connectify.repository.EventAdminRecordRepository;
import com.connectify.repository.EventRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDateTime;

@Configuration
@Profile("!prod")
public class DemoClientTimelineSeed {

    @Bean
    CommandLineRunner clientTimeline(EventRepository events, EventAdminRecordRepository records) {
        return args -> {
            Event event = events.findAll().stream()
                    .filter(item -> "Concierto Horizonte Andino".equals(item.getTitle()))
                    .findFirst().orElse(null);
            if (event == null) return;
            boolean exists = records.findByEventIdOrderByCreatedAtDesc(event.getId()).stream()
                    .anyMatch(record -> record.getDescription() != null && record.getDescription().contains("Compra Cliente de referencia"));
            if (exists) return;
            EventAdminRecord record = new EventAdminRecord();
            record.setEvent(event);
            record.setType(EventAdminRecordType.ADMIN_COPY);
            record.setDescription("Compra Cliente de referencia: 2 entradas General agregadas al carrito, checkout confirmado y tickets QR emitidos.");
            record.setCreatedAt(LocalDateTime.now());
            records.save(record);
        };
    }
}
