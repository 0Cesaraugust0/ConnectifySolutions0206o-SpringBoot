package com.connectify.repository;

import com.connectify.entity.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {

    List<TicketType> findByEventIdOrderByPriceAsc(Long eventId);

    List<TicketType> findByEventIdAndActiveTrueOrderByPriceAsc(Long eventId);
}
