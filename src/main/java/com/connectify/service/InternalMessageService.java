package com.connectify.service;

import com.connectify.entity.Event;
import com.connectify.entity.InternalMessage;
import com.connectify.entity.MessagePriority;
import com.connectify.entity.MessageStatus;
import com.connectify.entity.MessageType;
import com.connectify.entity.Role;
import com.connectify.repository.InternalMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InternalMessageService {

    private final InternalMessageRepository internalMessageRepository;
    private final EventService eventService;

    public InternalMessageService(InternalMessageRepository internalMessageRepository, EventService eventService) {
        this.internalMessageRepository = internalMessageRepository;
        this.eventService = eventService;
    }

    public List<InternalMessage> inbox(Role targetRole) {
        return internalMessageRepository.findByTargetRoleOrderByCreatedAtDesc(targetRole);
    }

    public List<InternalMessage> sent(String senderEmail) {
        return internalMessageRepository.findBySenderEmailOrderByCreatedAtDesc(senderEmail);
    }

    public long unread(Role targetRole) {
        return internalMessageRepository.countByTargetRoleAndReadByTargetFalse(targetRole);
    }

    public InternalMessage findById(Long id) {
        return internalMessageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Mensaje no encontrado"));
    }

    @Transactional
    public InternalMessage create(String senderName,
                                  String senderEmail,
                                  Role senderRole,
                                  Role targetRole,
                                  MessageType type,
                                  MessagePriority priority,
                                  String subject,
                                  String body,
                                  Long relatedEventId) {
        InternalMessage message = new InternalMessage();
        message.setSenderName(senderName);
        message.setSenderEmail(senderEmail);
        message.setSenderRole(senderRole);
        message.setTargetRole(targetRole);
        message.setType(type);
        message.setPriority(priority);
        message.setSubject(subject);
        message.setBody(body);
        message.setStatus(MessageStatus.OPEN);
        message.setCreatedAt(LocalDateTime.now());
        message.setUpdatedAt(LocalDateTime.now());

        if (relatedEventId != null) {
            Event event = eventService.findById(relatedEventId).orElse(null);
            message.setRelatedEvent(event);
        }

        return internalMessageRepository.save(message);
    }

    @Transactional
    public InternalMessage updateStatus(Long id, MessageStatus status) {
        InternalMessage message = findById(id);
        message.setStatus(status);
        message.setUpdatedAt(LocalDateTime.now());
        return internalMessageRepository.save(message);
    }

    @Transactional
    public InternalMessage markAsRead(Long id) {
        InternalMessage message = findById(id);
        message.setReadByTarget(true);
        message.setUpdatedAt(LocalDateTime.now());
        return internalMessageRepository.save(message);
    }
}
