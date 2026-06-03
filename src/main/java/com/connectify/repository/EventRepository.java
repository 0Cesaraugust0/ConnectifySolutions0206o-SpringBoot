package com.connectify.repository;

import com.connectify.entity.Event;
import com.connectify.entity.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByStatusOrderByEventDateAsc(EventStatus status);

    List<Event> findByTitleContainingIgnoreCaseAndStatusOrderByEventDateAsc(String title, EventStatus status);

    List<Event> findByCityContainingIgnoreCaseAndStatusOrderByEventDateAsc(String city, EventStatus status);

    List<Event> findByCategoryNameAndStatusOrderByEventDateAsc(String categoryName, EventStatus status);
}
