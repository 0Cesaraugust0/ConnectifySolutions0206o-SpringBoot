package com.connectify.repository;

import com.connectify.entity.InternalMessage;
import com.connectify.entity.MessageStatus;
import com.connectify.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InternalMessageRepository extends JpaRepository<InternalMessage, Long> {

    List<InternalMessage> findByTargetRoleOrderByCreatedAtDesc(Role targetRole);

    List<InternalMessage> findBySenderEmailOrderByCreatedAtDesc(String senderEmail);

    List<InternalMessage> findByTargetRoleAndStatusOrderByCreatedAtDesc(Role targetRole, MessageStatus status);

    List<InternalMessage> findByRelatedEventIdOrderByCreatedAtDesc(Long eventId);

    long countByTargetRoleAndReadByTargetFalse(Role targetRole);
}
