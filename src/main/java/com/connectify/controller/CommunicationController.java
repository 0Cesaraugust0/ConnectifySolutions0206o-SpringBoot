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

import java.util.ArrayList;
import java.util.List;

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
    public String inbox(@RequestParam(required = false) String box,
                        @RequestParam(required = false) Role from,
                        Authentication authentication,
                        Model model) {
        Role currentRole = currentRole(authentication);
        String email = authentication != null ? authentication.getName() : "system@connectify.com";

        if (currentRole == Role.CLIENT) {
            model.addAttribute("supportMode", true);
            model.addAttribute("role", Role.CLIENT);
            model.addAttribute("messages", messageService.sent(email));
            model.addAttribute("unread", 0);
            model.addAttribute("currentRole", currentRole);
            model.addAttribute("roleLabel", "Buzon de Mensajeria");
            model.addAttribute("roleDescription", "Consultas del Cliente hacia Soporte Connectify.");
            model.addAttribute("clientSupportCount", 0);
            model.addAttribute("customerInboxMode", false);
            model.addAttribute("operationsMode", false);
            return "communications/inbox";
        }

        // Cada cuenta consulta solamente el buzon de su propio rol.
        Role inboxRole = currentRole;
        List<InternalMessage> messages = messageService.inbox(inboxRole);
        boolean customerInboxMode = currentRole == Role.ADMIN
                && (from == Role.CLIENT || "customers".equalsIgnoreCase(box));
        boolean operationsMode = currentRole == Role.ADMIN
                && !customerInboxMode
                && ("operations".equalsIgnoreCase(box) || (from != null && from != Role.ADMIN));
        Role selectedSource = customerInboxMode ? Role.CLIENT : from != null ? from : currentRole;

        if (customerInboxMode) {
            messages = messages.stream()
                    .filter(this::isCustomerSupport)
                    .toList();
        } else if (from != null && from != currentRole) {
            messages = messages.stream()
                    .filter(message -> message.getSenderRole() == from)
                    .toList();
        } else if (operationsMode) {
            messages = messages.stream()
                    .filter(message -> !isCustomerSupport(message))
                    .toList();
        }

        List<Role> sourceRoles = new ArrayList<>(List.of(
                Role.ADMIN, Role.ORGANIZER, Role.DESIGNER, Role.GATE_AGENT, Role.CLIENT));
        sourceRoles.remove(currentRole);
        sourceRoles.add(0, currentRole);
        List<MessageSourceOption> sourceOptions = sourceRoles.stream()
                .map(source -> new MessageSourceOption(source,
                        source == currentRole ? roleLabel(source) : senderLabel(source)))
                .toList();

        model.addAttribute("supportMode", false);
        model.addAttribute("role", inboxRole);
        model.addAttribute("currentRole", currentRole);
        model.addAttribute("ownerRoleName", roleName(currentRole));
        model.addAttribute("customerInboxMode", customerInboxMode);
        model.addAttribute("operationsMode", operationsMode);
        model.addAttribute("roleLabel", roleLabel(inboxRole));
        model.addAttribute("viewTitle", selectedSource == currentRole
                ? roleLabel(currentRole) : senderLabel(selectedSource));
        model.addAttribute("roleDescription", selectedSource == currentRole
                ? roleDescription(inboxRole)
                : "Mensajes enviados por " + senderOrigin(selectedSource) + " a este buzon.");
        model.addAttribute("selectedSource", selectedSource);
        model.addAttribute("sourceOptions", sourceOptions);
        model.addAttribute("messages", messages);
        model.addAttribute("unread", messages.stream().filter(message -> !message.isReadByTarget()).count());
        model.addAttribute("clientSupportCount", messages.stream()
                .filter(message -> message.getSenderRole() == Role.CLIENT && message.getType() == MessageType.CUSTOMER_SUPPORT)
                .count());
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
        model.addAttribute("customerInboxMode", currentRole == Role.ADMIN && isCustomerSupport(message));
        model.addAttribute("operationsMode", currentRole == Role.ADMIN && !isCustomerSupport(message));
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

    private boolean isCustomerSupport(InternalMessage message) {
        return message.getSenderRole() == Role.CLIENT && message.getType() == MessageType.CUSTOMER_SUPPORT;
    }

    private String roleLabel(Role role) {
        return switch (role) {
            case ADMIN -> "Buzon del Administrador";
            case ORGANIZER -> "Buzon del Organizador";
            case DESIGNER -> "Buzon del Disenador";
            case GATE_AGENT -> "Buzon de Puerta";
            case CLIENT -> "Buzon del Cliente";
            case DEVELOPER -> "Buzon tecnico";
        };
    }

    private String roleName(Role role) {
        return switch (role) {
            case ADMIN -> "Administrador";
            case ORGANIZER -> "Organizador";
            case DESIGNER -> "Disenador";
            case GATE_AGENT -> "Puerta";
            case CLIENT -> "Cliente";
            case DEVELOPER -> "Developer";
        };
    }

    private String senderLabel(Role role) {
        return switch (role) {
            case ADMIN -> "Mensajeria del Administrador";
            case ORGANIZER -> "Mensajeria del Organizador";
            case DESIGNER -> "Mensajeria del Disenador";
            case GATE_AGENT -> "Mensajeria de Puerta";
            case CLIENT -> "Mensajeria de los Clientes";
            case DEVELOPER -> "Mensajeria tecnica";
        };
    }

    private String senderOrigin(Role role) {
        return switch (role) {
            case ADMIN -> "el Administrador";
            case ORGANIZER -> "el Organizador";
            case DESIGNER -> "el Disenador";
            case GATE_AGENT -> "el equipo de Puerta";
            case CLIENT -> "los Clientes";
            case DEVELOPER -> "el Developer";
        };
    }

    private String roleDescription(Role role) {
        return switch (role) {
            case ADMIN -> "Recibe soporte de clientes y solicitudes internas de revision.";
            case ORGANIZER -> "Recibe observaciones, aprobaciones y respuestas del Administrador o Disenador.";
            case DESIGNER -> "Recibe solicitudes visuales y feedback de eventos.";
            case GATE_AGENT -> "Recibe instrucciones para validacion QR y control de acceso.";
            case CLIENT -> "Consultas enviadas por el cliente al soporte.";
            case DEVELOPER -> "Mensajes tecnicos de accesos y permisos.";
        };
    }

    private record MessageSourceOption(Role role, String label) {
    }
}
