package com.connectify.repository;

import com.connectify.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByCode(String code);

    List<Ticket> findByPurchaseId(Long purchaseId);

    List<Ticket> findByAttendeeEmailOrderByGeneratedAtDesc(String attendeeEmail);
}
