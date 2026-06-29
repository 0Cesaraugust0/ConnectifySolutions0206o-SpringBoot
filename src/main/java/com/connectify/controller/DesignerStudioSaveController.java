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
@RequestMapping("/dashboard/designer/studio")
public class DesignerStudioSaveController {

    private final EventRepository events;
    private final EventPresentationSettingsRepository settings;
    private final EventAdminRecordRepository records;

    public DesignerStudioSaveController(EventRepository events,
                                       EventPresentationSettingsRepository settings,
                                       EventAdminRecordRepository records) {
        this.events = events;
        this.settings = settings;
        this.records = records;
    }

    @PostMapping("/{id}/save")
    public String save(@PathVariable Long id,
                       @RequestParam EventDesignTemplate designTemplate,
                       @RequestParam(defaultValue = "#4f46e5") String primaryColor,
                       @RequestParam(defaultValue = "#8b5cf6") String accentColor,
                       @RequestParam(required = false) String highlightText,
                       @RequestParam(required = false, defaultValue = "false") boolean showGallery,
                       @RequestParam(required = false, defaultValue = "false") boolean showOrganizer,
                       Authentication authentication) {
        Event event = events.findById(id).orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
        if (event.getStatus() == EventStatus.APPROVED || event.getStatus() == EventStatus.PUBLISHED) {
            return "redirect:/dashboard/designer?eventId=" + id + "&locked=true";
        }

        event.setDesignTemplate(designTemplate);
        event.setDesignEnabled(true);
        events.save(event);

        EventPresentationSettings profile = settings.findByEventId(id).orElseGet(EventPresentationSettings::new);
        profile.setEvent(event);
        profile.setPrimaryColor(safeColor(primaryColor, "#4f46e5"));
        profile.setAccentColor(safeColor(accentColor, "#8b5cf6"));
        profile.setHighlightText(text(highlightText, "Una experiencia diseñada para conectar con tu público."));
        profile.setShowGallery(showGallery);
        profile.setShowOrganizer(showOrganizer);
        profile.setUpdatedAt(LocalDateTime.now());
        settings.save(profile);

        EventAdminRecord record = new EventAdminRecord();
        record.setEvent(event);
        record.setType(EventAdminRecordType.ADMIN_COPY);
        record.setDescription("Propuesta visual guardada por " + designer(authentication)
                + "\nPlantilla: " + designTemplate.getLabel()
                + "\nColor principal: " + profile.getPrimaryColor()
                + "\nColor de acento: " + profile.getAccentColor()
                + "\nEntradas visibles: " + (showGallery ? "Sí" : "No")
                + "\nOrganizador visible: " + (showOrganizer ? "Sí" : "No"));
        record.setCreatedAt(LocalDateTime.now());
        records.save(record);

        return "redirect:/dashboard/designer?eventId=" + id + "&saved=true";
    }

    private String safeColor(String color, String fallback) {
        return color != null && color.matches("#[0-9a-fA-F]{6}") ? color : fallback;
    }

    private String text(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String designer(Authentication authentication) {
        return authentication == null || authentication.getName() == null ? "Diseñador" : authentication.getName();
    }
}
