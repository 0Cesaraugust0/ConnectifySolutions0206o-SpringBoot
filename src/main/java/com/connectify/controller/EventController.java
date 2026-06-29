package com.connectify.controller;

import com.connectify.entity.Event;
import com.connectify.entity.EventPresentationSettings;
import com.connectify.entity.EventStatus;
import com.connectify.repository.EventPresentationSettingsRepository;
import com.connectify.repository.TicketTypeRepository;
import com.connectify.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;
    private final TicketTypeRepository ticketTypeRepository;
    private final EventPresentationSettingsRepository presentationSettingsRepository;

    public EventController(EventService eventService,
                           TicketTypeRepository ticketTypeRepository,
                           EventPresentationSettingsRepository presentationSettingsRepository) {
        this.eventService = eventService;
        this.ticketTypeRepository = ticketTypeRepository;
        this.presentationSettingsRepository = presentationSettingsRepository;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) String city,
                       @RequestParam(required = false) String category,
                       Model model,
                       Authentication authentication) {
        List<Event> events = eventService.findPublished(q, city, category);
        List<Long> eventIds = events.stream().map(Event::getId).toList();
        Map<Long, EventPresentationSettings> presentationByEventId = eventIds.isEmpty()
                ? Map.of()
                : presentationSettingsRepository.findByEvent_IdIn(eventIds).stream()
                .filter(setting -> setting.getEvent() != null && setting.getEvent().getId() != null)
                .collect(Collectors.toMap(setting -> setting.getEvent().getId(), Function.identity(), (first, ignored) -> first));

        model.addAttribute("events", events);
        model.addAttribute("presentationByEventId", presentationByEventId);
        model.addAttribute("q", q);
        model.addAttribute("city", city);
        model.addAttribute("category", category);
        addAccountNavigation(model, authentication);
        return "events/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, Authentication authentication) {
        Event event = eventService.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no encontrado"));
        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Evento no disponible en marketplace");
        }
        EventPresentationSettings presentation = presentationSettingsRepository.findByEventId(id).orElse(null);
        addDetailModel(event, presentation, model);
        model.addAttribute("previewMode", false);
        addAccountNavigation(model, authentication);
        return "events/detail";
    }

    private void addDetailModel(Event event, EventPresentationSettings presentation, Model model) {
        model.addAttribute("event", event);
        model.addAttribute("ticketTypes", ticketTypeRepository.findByEventIdAndActiveTrueOrderByPriceAsc(event.getId()));
        model.addAttribute("presentation", presentation);
        model.addAttribute("coverImageUrl", firstText(presentation == null ? null : presentation.getCoverImageUrl(), event.getImageUrl()));
        model.addAttribute("thumbnailImageUrl", firstText(presentation == null ? null : presentation.getThumbnailImageUrl(), event.getImageUrl()));
        model.addAttribute("presentationStyle", styleFor(presentation));
    }

    private void addAccountNavigation(Model model, Authentication authentication) {
        boolean signedIn = authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName());
        boolean clientAccount = signedIn && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_CLIENT".equals(authority.getAuthority()));
        model.addAttribute("signedIn", signedIn);
        model.addAttribute("clientAccount", clientAccount);
        model.addAttribute("accountEmail", signedIn ? authentication.getName() : "");
    }

    private String firstText(String preferred, String fallback) {
        if (preferred != null && !preferred.isBlank()) return preferred;
        return fallback == null ? "" : fallback;
    }

    private String styleFor(EventPresentationSettings presentation) {
        if (presentation == null) return "";
        return "--market-primary:" + safeColor(presentation.getPrimaryColor(), "#4338ca") + ";"
                + "--market-accent:" + safeColor(presentation.getAccentColor(), "#7c3aed") + ";"
                + "--market-cover-position:" + coverPosition(presentation.getCoverFocus()) + ";";
    }

    private String safeColor(String color, String fallback) {
        return color != null && color.matches("#[0-9a-fA-F]{6}") ? color : fallback;
    }

    private String coverPosition(String value) {
        if ("TOP".equals(value)) return "50% 18%";
        if ("BOTTOM".equals(value)) return "50% 82%";
        return "50% 50%";
    }
}
