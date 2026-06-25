package com.connectify.controller;

import com.connectify.entity.Event;
import com.connectify.entity.EventAdminRecord;
import com.connectify.entity.EventAdminRecordType;
import com.connectify.entity.EventStatus;
import com.connectify.entity.MessagePriority;
import com.connectify.entity.MessageType;
import com.connectify.entity.Role;
import com.connectify.repository.EventAdminRecordRepository;
import com.connectify.repository.EventRepository;
import com.connectify.repository.TicketTypeRepository;
import com.connectify.service.InternalMessageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/dashboard/admin/events")
public class AdminDashboardController {

    private final EventRepository eventRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final EventAdminRecordRepository recordRepository;
    private final InternalMessageService messageService;

    public AdminDashboardController(EventRepository eventRepository,
                                    TicketTypeRepository ticketTypeRepository,
                                    EventAdminRecordRepository recordRepository,
                                    InternalMessageService messageService) {
        this.eventRepository = eventRepository;
        this.ticketTypeRepository = ticketTypeRepository;
        this.recordRepository = recordRepository;
        this.messageService = messageService;
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Event event = findEvent(id);
        List<EventAdminRecord> records = recordRepository.findByEventIdOrderByCreatedAtDesc(id);
        List<EventAdminRecord> changeRecords = records.stream()
                .filter(record -> record.getType() == EventAdminRecordType.UPDATED
                        || record.getType() == EventAdminRecordType.TICKET_TYPE_CREATED
                        || record.getType() == EventAdminRecordType.TICKET_TYPE_UPDATED)
                .toList();

        model.addAttribute("event", event);
        model.addAttribute("ticketTypes", ticketTypeRepository.findByEventIdOrderByPriceAsc(id));
        model.addAttribute("records", records);
        model.addAttribute("changeRecords", changeRecords);
        return "dashboard/admin/event-detail";
    }

    @PostMapping("/{id}/mark-ok")
    public String markOk(@PathVariable Long id,
                         @RequestParam(required = false, defaultValue = "Evento conforme para continuar revisión administrativa.") String note) {
        Event event = findEvent(id);
        event.setStatus(EventStatus.APPROVED);
        event.setDesignEnabled(false);
        eventRepository.save(event);

        createRecord(event, "Conforme: " + note + " Servicios del organizador y diseño bloqueados hasta nueva decisión administrativa.");
        messageService.create("Administrador", "admin@connectify.com", Role.ADMIN, Role.ORGANIZER,
                MessageType.EVENT_REVIEW, MessagePriority.NORMAL,
                "Evento aprobado: " + event.getTitle(),
                "La construcción fue aprobada y quedó bloqueada para nuevas ediciones. Aún no es visible al público. Nota: " + note,
                id);
        messageService.create("Administrador", "admin@connectify.com", Role.ADMIN, Role.DESIGNER,
                MessageType.DESIGN_FEEDBACK, MessagePriority.NORMAL,
                "Diseño aprobado y bloqueado: " + event.getTitle(),
                "La propuesta fue aprobada. Las acciones de diseño para este evento quedan bloqueadas hasta nueva decisión administrativa.",
                id);
        return "redirect:/dashboard/admin/events/" + id + "?ok=true";
    }

    @PostMapping("/{id}/observe")
    public String observe(@PathVariable Long id,
                          @RequestParam(required = false, defaultValue = "Evento observado. Requiere ajustes del organizador.") String note,
                          @RequestParam(required = false, defaultValue = "false") boolean notifyDesigner) {
        Event event = findEvent(id);
        event.setStatus(EventStatus.OBSERVED);
        event.setDesignEnabled(notifyDesigner);
        eventRepository.save(event);

        createRecord(event, "Observado: " + note + (notifyDesigner ? " Aviso enviado también al diseñador." : " Aviso enviado al organizador."));
        String subject = "Observación administrativa: " + event.getTitle();
        messageService.create("Administrador", "admin@connectify.com", Role.ADMIN, Role.ORGANIZER,
                MessageType.EVENT_REVIEW, MessagePriority.HIGH, subject,
                "El evento fue observado y requiere ajustes antes de volver a revisión. Detalle: " + note,
                id);

        if (notifyDesigner) {
            messageService.create("Administrador", "admin@connectify.com", Role.ADMIN, Role.DESIGNER,
                    MessageType.DESIGN_FEEDBACK, MessagePriority.NORMAL, subject,
                    "Se solicitó apoyo visual o de presentación para este evento. Detalle: " + note,
                    id);
        }
        return "redirect:/dashboard/admin/events/" + id + "?observed=true";
    }

    @PostMapping("/{id}/publish")
    public String publish(@PathVariable Long id,
                          @RequestParam(required = false, defaultValue = "Evento publicado en marketplace.") String note) {
        Event event = findEvent(id);
        if (event.getStatus() != EventStatus.APPROVED) {
            return "redirect:/dashboard/admin/events/" + id + "?publishDenied=true";
        }
        event.setStatus(EventStatus.PUBLISHED);
        eventRepository.save(event);
        createRecord(event, "Publicado: " + note);
        messageService.create("Administrador", "admin@connectify.com", Role.ADMIN, Role.ORGANIZER,
                MessageType.EVENT_REVIEW, MessagePriority.NORMAL,
                "Evento publicado: " + event.getTitle(),
                "El evento fue publicado en marketplace. " + note,
                id);
        return "redirect:/dashboard/admin/events/" + id + "?published=true";
    }

    @PostMapping("/{id}/unpublish")
    public String unpublish(@PathVariable Long id,
                            @RequestParam(required = false, defaultValue = "Evento retirado temporalmente de marketplace.") String note) {
        Event event = findEvent(id);
        if (event.getStatus() != EventStatus.PUBLISHED) {
            return "redirect:/dashboard/admin/events/" + id + "?unpublishDenied=true";
        }
        event.setStatus(EventStatus.APPROVED);
        eventRepository.save(event);
        createRecord(event, "Despublicado: " + note + " El evento permanece aprobado y bloqueado.");
        messageService.create("Administrador", "admin@connectify.com", Role.ADMIN, Role.ORGANIZER,
                MessageType.EVENT_REVIEW, MessagePriority.NORMAL,
                "Evento despublicado: " + event.getTitle(),
                "El evento fue retirado temporalmente del marketplace y permanece bloqueado en estado aprobado. " + note,
                id);
        return "redirect:/dashboard/admin/events/" + id + "?unpublished=true";
    }

    private Event findEvent(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
    }

    private void createRecord(Event event, String description) {
        EventAdminRecord record = new EventAdminRecord();
        record.setEvent(event);
        record.setType(EventAdminRecordType.ADMIN_COPY);
        record.setDescription(description);
        record.setCreatedAt(LocalDateTime.now());
        recordRepository.save(record);
    }
}
