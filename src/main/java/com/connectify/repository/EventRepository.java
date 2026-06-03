package com.connectify.repository;

import com.connectify.entity.Event;
import com.connectify.entity.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByStatusAndEventDateAfterOrderByEventDateAsc(EventStatus status, LocalDateTime now);

    List<Event> findByTitleContainingIgnoreCaseAndStatusAndEventDateAfterOrderByEventDateAsc(String title, EventStatus status, LocalDateTime now);

    List<Event> findByCityContainingIgnoreCaseAndStatusAndEventDateAfterOrderByEventDateAsc(String city, EventStatus status, LocalDateTime now);

    List<Event> findByCategoryNameAndStatusAndEventDateAfterOrderByEventDateAsc(String categoryName, EventStatus status, LocalDateTime now);
}
