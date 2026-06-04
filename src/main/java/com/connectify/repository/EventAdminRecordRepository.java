package com.connectify.repository;

import com.connectify.entity.EventAdminRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventAdminRecordRepository extends JpaRepository<EventAdminRecord, Long> {

    List<EventAdminRecord> findByEventIdOrderByCreatedAtDesc(Long eventId);
}
