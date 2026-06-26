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

    public AdminDashboardController(EventRepository eventRepository, TicketTypeRepository ticketTypeRepository,
                                    EventAdminRecordRepository recordRepository, InternalMessageService messageService) {
        this.eventRepository = eventRepository;
        this.ticketTypeRepository = ticketTypeRepository;
        this.recordRepository = recordRepository;
        this.messageService = messageService;
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Event event = findEvent(id);
        List<EventAdminRecord> records = recordRepository.findByEventIdOrderByCreatedAtDesc(id);
        model.addAttribute("event", event);
        model.addAttribute("ticketTypes", ticketTypeRepository.findByEventIdOrderByPriceAsc(id));
        model.addAttribute("records", records);
        model.addAttribute("changeRecords", records.stream().filter(record -> record.getType() == EventAdminRecordType.UPDATED || record.getType() == EventAdminRecordType.TICKET_TYPE_CREATED || record.getType() == EventAdminRecordType.TICKET_TYPE_UPDATED).toList());
        return "dashboard/admin/event-detail";
    }

    @GetMapping("/{id}/preview")
    public String preview(@PathVariable Long id, Model model) {
        Event event = findEvent(id);
        model.addAttribute("event", event);
        model.addAttribute("ticketTypes", ticketTypeRepository.findByEventIdAndActiveTrueOrderByPriceAsc(id));
        model.addAttribute("previewMode", true);
        model.addAttribute("adminReturnUrl", "/dashboard/admin/events/" + id);
        return "events/detail";
    }

    @PostMapping("/{id}/observe")
    public String observe(@PathVariable Long id, @RequestParam(defaultValue = "Evento observado. Requiere ajustes del organizador.") String note,
                          @RequestParam(defaultValue = "false") boolean notifyDesigner) {
        Event event = findEvent(id);
        if (!reviewable(event)) return denied(id);
        event.setStatus(EventStatus.OBSERVED);
        event.setDesignEnabled(notifyDesigner);
        eventRepository.save(event);
        record(event, "Observado: " + note);
        messageService.create("Administrador", "admin@connectify.com", Role.ADMIN, Role.ORGANIZER, MessageType.EVENT_REVIEW, MessagePriority.HIGH,
                "Observación administrativa: " + event.getTitle(), note, id);
        if (notifyDesigner) {
            messageService.create("Administrador", "admin@connectify.com", Role.ADMIN, Role.DESIGNER, MessageType.DESIGN_FEEDBACK, MessagePriority.NORMAL,
                    "Ajuste visual solicitado: " + event.getTitle(), note, id);
        }
        return "redirect:/dashboard/admin/events/" + id + "?observed=true";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, @RequestParam(defaultValue = "Construcción, datos y entradas conformes.") String note) {
        Event event = findEvent(id);
        if (!reviewable(event)) return denied(id);
        event.setStatus(EventStatus.APPROVED);
        event.setDesignEnabled(false);
        eventRepository.save(event);
        record(event, "Aprobado: " + note + " El evento queda bloqueado para edición.");
        messageService.create("Administrador", "admin@connectify.com", Role.ADMIN, Role.ORGANIZER, MessageType.EVENT_REVIEW, MessagePriority.NORMAL,
                "Evento aprobado: " + event.getTitle(), "La construcción fue aprobada y quedó bloqueada. Aún no está publicada.", id);
        return "redirect:/dashboard/admin/events/" + id + "?approved=true";
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id, @RequestParam(defaultValue = "La propuesta no cumple las condiciones de aprobación.") String note,
                         @RequestParam(defaultValue = "false") boolean notifyDesigner) {
        Event event = findEvent(id);
        if (!reviewable(event)) return denied(id);
        event.setStatus(EventStatus.REJECTED);
        event.setDesignEnabled(false);
        eventRepository.save(event);
        record(event, "Rechazado: " + note);
        messageService.create("Administrador", "admin@connectify.com", Role.ADMIN, Role.ORGANIZER, MessageType.EVENT_REVIEW, MessagePriority.HIGH,
                "Propuesta rechazada: " + event.getTitle(), note, id);
        if (notifyDesigner) {
            messageService.create("Administrador", "admin@connectify.com", Role.ADMIN, Role.DESIGNER, MessageType.DESIGN_FEEDBACK, MessagePriority.NORMAL,
                    "Propuesta rechazada: " + event.getTitle(), note, id);
        }
        return "redirect:/dashboard/admin/events/" + id + "?rejected=true";
    }

    @PostMapping("/{id}/publish")
    public String publish(@PathVariable Long id) {
        Event event = findEvent(id);
        if (event.getStatus() != EventStatus.APPROVED) return "redirect:/dashboard/admin/events/" + id + "?publishDenied=true";
        event.setStatus(EventStatus.PUBLISHED);
        eventRepository.save(event);
        record(event, "Publicado en marketplace.");
        return "redirect:/dashboard/admin/events/" + id + "?published=true";
    }

    @PostMapping("/{id}/unpublish")
    public String unpublish(@PathVariable Long id) {
        Event event = findEvent(id);
        if (event.getStatus() != EventStatus.PUBLISHED) return "redirect:/dashboard/admin/events/" + id + "?unpublishDenied=true";
        event.setStatus(EventStatus.APPROVED);
        eventRepository.save(event);
        record(event, "Despublicado. El evento queda aprobado y bloqueado.");
        return "redirect:/dashboard/admin/events/" + id + "?unpublished=true";
    }

    private boolean reviewable(Event event) { return event.getStatus() == EventStatus.PENDING_REVIEW || event.getStatus() == EventStatus.OBSERVED; }
    private String denied(Long id) { return "redirect:/dashboard/admin/events/" + id + "?decisionDenied=true"; }
    private Event findEvent(Long id) { return eventRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Evento no encontrado")); }
    private void record(Event event, String description) {
        EventAdminRecord record = new EventAdminRecord();
        record.setEvent(event); record.setType(EventAdminRecordType.ADMIN_COPY); record.setDescription(description); record.setCreatedAt(LocalDateTime.now());
        recordRepository.save(record);
    }
}
