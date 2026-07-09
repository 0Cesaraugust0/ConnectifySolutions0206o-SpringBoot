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
    public String inbox(@RequestParam(required = false) Role role,
                        Authentication authentication,
                        Model model) {
        Role currentRole = currentRole(authentication);
        String email = authentication != null ? authentication.getName() : "system@connectify.com";

        if (currentRole == Role.CLIENT) {
            model.addAttribute("supportMode", true);
            model.addAttribute("role", Role.CLIENT);
            model.addAttribute("messages", messageService.sent(email));
            model.addAttribute("unread", 0);
            return "communications/inbox";
        }

        Role inboxRole = role != null ? role : currentRole;
        model.addAttribute("supportMode", false);
        model.addAttribute("role", inboxRole);
        model.addAttribute("messages", messageService.inbox(inboxRole));
        model.addAttribute("unread", messageService.unread(inboxRole));
        return "communications/inbox";
    }

    @GetMapping("/sent")
    public String sent(Authentication authentication, Model model) {
        String email = authentication != null ? authentication.getName() : "system@connectify.com";
        model.addAttribute("messages", messageService.sent(email));
        return "communications/sent";
    }

    @GetMapping("/new")
    public String newMessage(Authentication authentication, Model model) {
        Role currentRole = currentRole(authentication);
        boolean supportMode = currentRole == Role.CLIENT;

        model.addAttribute("supportMode", supportMode);
        model.addAttribute("senderRole", supportMode ? Role.CLIENT : currentRole);
        model.addAttribute("senderEmail", authentication != null ? authentication.getName() : "");
        model.addAttribute("roles", Role.values());
        model.addAttribute("types", MessageType.values());
        model.addAttribute("priorities", MessagePriority.values());
        model.addAttribute("events", eventService.findAll());
        return "communications/form";
    }

    @PostMapping
    public String create(@RequestParam String senderName,
                         @RequestParam(required = false) String senderEmail,
                         @RequestParam(required = false) Role senderRole,
                         @RequestParam(required = false) Role targetRole,
                         @RequestParam(required = false) MessageType type,
                         @RequestParam(required = false) MessagePriority priority,
                         @RequestParam String subject,
                         @RequestParam String body,
                         @RequestParam(required = false) Long relatedEventId,
                         Authentication authentication) {
        Role currentRole = currentRole(authentication);

        if (currentRole == Role.CLIENT) {
            String email = authentication != null ? authentication.getName() : senderEmail;
            messageService.create(senderName, email, Role.CLIENT, Role.ADMIN, MessageType.CUSTOMER_SUPPORT,
                    MessagePriority.NORMAL, subject, body, null);
            return "redirect:/communications?sent=true";
        }

        Role safeSenderRole = senderRole != null ? senderRole : currentRole;
        Role safeTargetRole = targetRole != null ? targetRole : Role.ADMIN;
        MessageType safeType = type != null ? type : MessageType.GENERAL_COORDINATION;
        MessagePriority safePriority = priority != null ? priority : MessagePriority.NORMAL;
        String safeSenderEmail = senderEmail == null || senderEmail.isBlank()
                ? authentication != null ? authentication.getName() : "system@connectify.com"
                : senderEmail;

        messageService.create(senderName, safeSenderEmail, safeSenderRole, safeTargetRole, safeType,
                safePriority, subject, body, relatedEventId);
        return "redirect:/communications?role=" + safeTargetRole.name();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Authentication authentication, Model model) {
        Role currentRole = currentRole(authentication);
        InternalMessage message = currentRole == Role.CLIENT
                ? messageService.findById(id)
                : messageService.markAsRead(id);

        if (currentRole == Role.CLIENT && !message.getSenderEmail().equalsIgnoreCase(authentication.getName())) {
            return "redirect:/communications";
        }

        model.addAttribute("supportMode", currentRole == Role.CLIENT);
        model.addAttribute("message", message);
        model.addAttribute("statuses", MessageStatus.values());
        return "communications/detail";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam MessageStatus status,
                               Authentication authentication) {
        if (currentRole(authentication) == Role.CLIENT) {
            return "redirect:/communications";
        }
        messageService.updateStatus(id, status);
        return "redirect:/communications/" + id;
    }

    private Role currentRole(Authentication authentication) {
        if (authentication == null) {
            return Role.ADMIN;
        }
        if (hasRole(authentication, "ROLE_DEVELOPER")) return Role.DEVELOPER;
        if (hasRole(authentication, "ROLE_ADMIN")) return Role.ADMIN;
        if (hasRole(authentication, "ROLE_ORGANIZER")) return Role.ORGANIZER;
        if (hasRole(authentication, "ROLE_CLIENT")) return Role.CLIENT;
        if (hasRole(authentication, "ROLE_DESIGNER")) return Role.DESIGNER;
        if (hasRole(authentication, "ROLE_GATE_AGENT")) return Role.GATE_AGENT;
        return Role.ADMIN;
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }
}
