package com.connectify.controller;

import com.connectify.entity.Event;
import com.connectify.entity.EventAdminRecord;
import com.connectify.entity.EventAdminRecordType;
import com.connectify.entity.EventDesignTemplate;
import com.connectify.entity.EventPresentationSettings;
import com.connectify.entity.EventStatus;
import com.connectify.entity.MessagePriority;
import com.connectify.entity.MessageType;
import com.connectify.entity.Role;
import com.connectify.repository.EventAdminRecordRepository;
import com.connectify.repository.EventPresentationSettingsRepository;
import com.connectify.repository.EventRepository;
import com.connectify.service.InternalMessageService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.Set;

@Controller
@RequestMapping("/dashboard/designer")
public class DesignerPresentationController {

    private static final Set<String> COVER_FOCUS = Set.of("TOP", "CENTER", "BOTTOM");
    private static final Set<String> CONTENT_FOCUS = Set.of("BALANCED", "SALES", "INFORMATION");
    private static final Set<String> INFO_POSITION = Set.of("STANDARD", "SIDEBAR");

    private final EventRepository eventRepository;
    private final EventAdminRecordRepository recordRepository;
    private final EventPresentationSettingsRepository presentationSettingsRepository;
    private final InternalMessageService messageService;

    public DesignerPresentationController(EventRepository eventRepository,
                                          EventAdminRecordRepository recordRepository,
                                          EventPresentationSettingsRepository presentationSettingsRepository,
                                          InternalMessageService messageService) {
        this.eventRepository = eventRepository;
        this.recordRepository = recordRepository;
        this.presentationSettingsRepository = presentationSettingsRepository;
        this.messageService = messageService;
    }

    @PostMapping("/events/{id}/presentation")
    public String savePresentation(@PathVariable Long id,
                                   @RequestParam EventDesignTemplate designTemplate,
                                   @RequestParam(defaultValue = "#4f46e5") String primaryColor,
                                   @RequestParam(defaultValue = "#8b5cf6") String accentColor,
                                   @RequestParam(required = false) String coverImageUrl,
                                   @RequestParam(required = false) String thumbnailImageUrl,
                                   @RequestParam(defaultValue = "CENTER") String coverFocus,
                                   @RequestParam(defaultValue = "BALANCED") String contentFocus,
                                   @RequestParam(defaultValue = "STANDARD") String infoPosition,
                                   @RequestParam(required = false) String highlightText,
                                   @RequestParam(required = false, defaultValue = "false") boolean showGallery,
                                   @RequestParam(required = false, defaultValue = "false") boolean showOrganizer,
                                   @RequestParam(required = false, defaultValue = "false") boolean showAgenda,
                                   @RequestParam(required = false) String agendaText,
                                   @RequestParam(required = false, defaultValue = "false") boolean showBenefits,
                                   @RequestParam(required = false) String benefitsText,
                                   @RequestParam(required = false, defaultValue = "false") boolean showSponsors,
                                   @RequestParam(required = false) String sponsorsText,
                                   Authentication authentication) {
        Event event = findEditableEvent(id);

        event.setDesignTemplate(designTemplate);
        event.setDesignEnabled(true);
        eventRepository.save(event);

        EventPresentationSettings presentation = presentationSettingsRepository.findByEventId(id)
                .orElseGet(EventPresentationSettings::new);
        if (presentation.getEvent() == null) {
            presentation.setEvent(event);
        }
        presentation.setPrimaryColor(safeColor(primaryColor, "#4f46e5"));
        presentation.setAccentColor(safeColor(accentColor, "#8b5cf6"));
        presentation.setCoverImageUrl(safeUrl(coverImageUrl));
        presentation.setThumbnailImageUrl(safeUrl(thumbnailImageUrl));
        presentation.setCoverFocus(safeChoice(coverFocus, COVER_FOCUS, "CENTER"));
        presentation.setContentFocus(safeChoice(contentFocus, CONTENT_FOCUS, "BALANCED"));
        presentation.setInfoPosition(safeChoice(infoPosition, INFO_POSITION, "STANDARD"));
        presentation.setHighlightText(optionalText(highlightText));
        presentation.setShowGallery(showGallery);
        presentation.setShowOrganizer(showOrganizer);
        presentation.setShowAgenda(showAgenda);
        presentation.setAgendaText(optionalText(agendaText));
        presentation.setShowBenefits(showBenefits);
        presentation.setBenefitsText(optionalText(benefitsText));
        presentation.setShowSponsors(showSponsors);
        presentation.setSponsorsText(optionalText(sponsorsText));
        presentationSettingsRepository.save(presentation);

        EventAdminRecord record = new EventAdminRecord();
        record.setEvent(event);
        record.setType(EventAdminRecordType.ADMIN_COPY);
        record.setDescription("Propuesta visual guardada por Diseñador (" + safeName(authentication) + "):\n"
                + "Plantilla: " + designTemplate.getLabel() + "\n"
                + "Paleta: " + presentation.getPrimaryColor() + " / " + presentation.getAccentColor() + "\n"
                + "Portada visual: " + valueOr(presentation.getCoverImageUrl(), "imagen del evento") + "\n"
                + "Encuadre: " + presentation.getCoverFocus() + " | Enfoque: " + presentation.getContentFocus() + "\n"
                + "Bloques: entradas visuales=" + yesNo(showGallery)
                + ", organizador=" + yesNo(showOrganizer)
                + ", agenda=" + yesNo(showAgenda)
                + ", beneficios=" + yesNo(showBenefits)
                + ", sponsors=" + yesNo(showSponsors) + "\n"
                + "Texto destacado: " + clean(highlightText));
        record.setCreatedAt(LocalDateTime.now());
        recordRepository.save(record);

        return "redirect:/dashboard/designer?eventId=" + id + "&saved=true";
    }

    @PostMapping("/events/{id}/send-to-organizer")
    public String sendToOrganizer(@PathVariable Long id,
                                  @RequestParam(required = false) String note,
                                  Authentication authentication) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
        String message = optionalText(note);
        if (message == null) {
            message = "La propuesta visual fue actualizada. Revisa la plantilla, portada y composición antes de enviarla a revisión administrativa.";
        }

        messageService.create("Diseñador", safeName(authentication), Role.DESIGNER, Role.ORGANIZER,
                MessageType.DESIGN_FEEDBACK, MessagePriority.NORMAL,
                "Actualización visual: " + event.getTitle(), message, id);

        EventAdminRecord record = new EventAdminRecord();
        record.setEvent(event);
        record.setType(EventAdminRecordType.ADMIN_COPY);
        record.setDescription("Diseñador envió una actualización visual al Organizador:\n" + message);
        record.setCreatedAt(LocalDateTime.now());
        recordRepository.save(record);

        return "redirect:/dashboard/designer?eventId=" + id + "&sent=true";
    }

    private Event findEditableEvent(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
        if (event.getStatus() == EventStatus.APPROVED || event.getStatus() == EventStatus.PUBLISHED) {
            throw new IllegalStateException("El evento ya fue aprobado o publicado y no admite cambios visuales");
        }
        return event;
    }

    private String safeName(Authentication authentication) {
        return authentication == null || authentication.getName() == null || authentication.getName().isBlank()
                ? "Diseñador" : authentication.getName();
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? "Sin texto adicional" : value.trim();
    }

    private String optionalText(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String safeColor(String value, String fallback) {
        return value != null && value.matches("#[0-9a-fA-F]{6}") ? value : fallback;
    }

    private String safeUrl(String value) {
        if (value == null || value.isBlank()) return null;
        String normalized = value.trim();
        return normalized.startsWith("https://") || normalized.startsWith("http://") ? normalized : null;
    }

    private String safeChoice(String value, Set<String> allowed, String fallback) {
        return value != null && allowed.contains(value.toUpperCase()) ? value.toUpperCase() : fallback;
    }

    private String yesNo(boolean value) {
        return value ? "visible" : "oculto";
    }

    private String valueOr(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
