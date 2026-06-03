package com.connectify.service;

import com.connectify.entity.Event;
import com.connectify.entity.EventStatus;
import com.connectify.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<Event> findPublished(String query, String city, String category) {
        if (StringUtils.hasText(query)) {
            return eventRepository.findByTitleContainingIgnoreCaseAndStatusOrderByEventDateAsc(query.trim(), EventStatus.PUBLISHED);
        }

        if (StringUtils.hasText(city)) {
            return eventRepository.findByCityContainingIgnoreCaseAndStatusOrderByEventDateAsc(city.trim(), EventStatus.PUBLISHED);
        }

        if (StringUtils.hasText(category)) {
            return eventRepository.findByCategoryNameAndStatusOrderByEventDateAsc(category.trim(), EventStatus.PUBLISHED);
        }

        return eventRepository.findByStatusOrderByEventDateAsc(EventStatus.PUBLISHED);
    }

    public List<Event> findAll() {
        return eventRepository.findAll();
    }

    public Optional<Event> findById(Long id) {
        return eventRepository.findById(id);
    }
}
