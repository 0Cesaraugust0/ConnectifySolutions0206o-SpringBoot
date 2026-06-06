package com.connectify.service;

import com.connectify.entity.Event;
import com.connectify.entity.EventStatus;
import com.connectify.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<Event> findPublished(String query, String city, String category) {
        LocalDateTime now = LocalDateTime.now();

        if (StringUtils.hasText(query)) {
            return eventRepository.findByTitleContainingIgnoreCaseAndStatusAndEventDateAfterOrderByEventDateAsc(query.trim(), EventStatus.PUBLISHED, now);
        }

        if (StringUtils.hasText(city)) {
            return eventRepository.findByCityContainingIgnoreCaseAndStatusAndEventDateAfterOrderByEventDateAsc(city.trim(), EventStatus.PUBLISHED, now);
        }

        if (StringUtils.hasText(category)) {
            return eventRepository.findByCategoryNameAndStatusAndEventDateAfterOrderByEventDateAsc(category.trim(), EventStatus.PUBLISHED, now);
        }

        return eventRepository.findByStatusAndEventDateAfterOrderByEventDateAsc(EventStatus.PUBLISHED, now);
    }

    public List<Event> findAll() {
        return eventRepository.findAll();
    }

    public Optional<Event> findById(Long id) {
        return eventRepository.findById(id);
    }

    public Event save(Event event) {
        return eventRepository.save(event);
    }

    @Transactional
    public Event updateGateAccess(Long id, String gateAccessCode, String gatePassword) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
        event.setGateAccessCode(gateAccessCode);
        event.setGatePassword(gatePassword);
        return eventRepository.save(event);
    }
}
