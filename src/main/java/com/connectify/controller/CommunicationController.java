package com.connectify.controller;

import com.connectify.entity.InternalMessage;
import com.connectify.entity.MessagePriority;
import com.connectify.entity.MessageStatus;
import com.connectify.entity.MessageType;
import com.connectify.entity.Role;
import com.connectify.service.EventService;
import com.connectify.service.InternalMessageService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/communications")
public class CommunicationController {

    private final InternalMessageService messageService;
    private final EventService eventService;

    public CommunicationController(InternalMessageService messageService, EventService eventService) {
        this.messageService = messageService;
        this.eventService = eventService;
    }

    @GetMapping
    public String inbox(@RequestParam(defaultValue = "ADMIN") Role role, Model model) {
        model.addAttribute("role", role);
        model.addAttribute("messages", messageService.inbox(role));
        model.addAttribute("unread", messageService.unread(role));
        return "communications/inbox";
    }

    @GetMapping("/sent")
    public String sent(Authentication authentication, Model model) {
        String email = authentication != null ? authentication.getName() : "system@connectify.com";
        model.addAttribute("messages", messageService.sent(email));
        return "communications/sent";
    }

    @GetMapping("/new")
    public String newMessage(Model model) {
        model.addAttribute("roles", Role.values());
        model.addAttribute("types", MessageType.values());
        model.addAttribute("priorities", MessagePriority.values());
        model.addAttribute("events", eventService.findAll());
        return "communications/form";
    }

    @PostMapping
    public String create(@RequestParam String senderName,
                         @RequestParam String senderEmail,
                         @RequestParam Role senderRole,
                         @RequestParam Role targetRole,
                         @RequestParam MessageType type,
                         @RequestParam MessagePriority priority,
                         @RequestParam String subject,
                         @RequestParam String body,
                         @RequestParam(required = false) Long relatedEventId) {
        messageService.create(senderName, senderEmail, senderRole, targetRole, type, priority, subject, body, relatedEventId);
        return "redirect:/communications?role=" + targetRole.name();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        InternalMessage message = messageService.markAsRead(id);
        model.addAttribute("message", message);
        model.addAttribute("statuses", MessageStatus.values());
        return "communications/detail";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam MessageStatus status) {
        messageService.updateStatus(id, status);
        return "redirect:/communications/" + id;
    }
}
