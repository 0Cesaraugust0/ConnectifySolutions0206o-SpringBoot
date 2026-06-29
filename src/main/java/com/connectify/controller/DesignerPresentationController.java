package com.connectify.controller;

import com.connectify.entity.Event;
import com.connectify.entity.EventAdminRecord;
import com.connectify.entity.EventAdminRecordType;
import com.connectify.entity.EventDesignTemplate;
import com.connectify.entity.EventPresentationSettings;
import com.connectify.entity.EventStatus;
import com.connectify.repository.EventAdminRecordRepository;
import com.connectify.repository.EventPresentationSettingsRepository;
import com.connectify.repository.EventRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/dashboard/designer")
public class DesignerPresentationController {

    private final EventRepository eventRepository;
    private final EventAdminRecordRepository recordRepository;
    private final EventPresentationSettingsRepository presentationSettingsRepository;

    public DesignerPresentationController(EventRepository eventRepository,
                                          EventAdminRecordRepository recordRepository,
                                          EventPresentationSettingsRepository presentationSettingsRepository) {
        this.eventRepository = eventRepository;
        this.recordRepository = recordRepository;
        this.presentationSettingsRepository = presentationSettingsRepository;
    }

    @PostMapping("/events/{id}/presentation")
    public String savePresentation(@PathVariable Long id,
                                   @RequestParam EventDesignTemplate designTemplate,
                                   @RequestParam(defaultValue = "#4f46e5") String primaryColor,
                                   @RequestParam(defaultValue = "#8b5cf6") String accentColor,
                                   @RequestParam(required = false) String highlightText,
                                   @RequestParam(required = false, defaultValue = "false") boolean showGallery,
                                   @RequestParam(required = false, defaultValue = "false") boolean showOrganizer,
                                   Authentication authentication) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));

        if (event.getStatus() == EventStatus.APPROVED || event.getStatus() == EventStatus.PUBLISHED) {
            return "redirect:/dashboard/designer?eventId=" + id + "&locked=true";
        }

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
        presentation.setHighlightText(optionalText(highlightText));
        presentation.setShowGallery(showGallery);
        presentation.setShowOrganizer(showOrganizer);
        presentationSettingsRepository.save(presentation);

        EventAdminRecord record = new EventAdminRecord();
        record.setEvent(event);
        record.setType(EventAdminRecordType.ADMIN_COPY);
        record.setDescription("Propuesta visual guardada por Diseñador (" + safeName(authentication) + "):\n"
                + "Plantilla: " + designTemplate.getLabel() + "\n"
                + "Color principal: " + presentation.getPrimaryColor() + "\n"
                + "Color de acento: " + presentation.getAccentColor() + "\n"
                + "Tarjetas de entradas: " + (showGallery ? "Visibles" : "Ocultas en la composición") + "\n"
                + "Bloque organizador: " + (showOrganizer ? "Visible" : "Oculto") + "\n"
                + "Texto destacado: " + clean(highlightText));
        record.setCreatedAt(LocalDateTime.now());
        recordRepository.save(record);

        return "redirect:/dashboard/designer?eventId=" + id + "&saved=true";
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
}
