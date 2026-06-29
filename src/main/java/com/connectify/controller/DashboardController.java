package com.connectify.controller;

import com.connectify.entity.Event;
import com.connectify.entity.EventDesignTemplate;
import com.connectify.entity.EventPresentationSettings;
import com.connectify.entity.EventStatus;
import com.connectify.repository.EventPresentationSettingsRepository;
import com.connectify.repository.EventRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Comparator;
import java.util.List;

@Controller
public class DashboardController {

    private final EventRepository eventRepository;
    private final EventPresentationSettingsRepository presentationSettingsRepository;

    public DashboardController(EventRepository eventRepository,
                               EventPresentationSettingsRepository presentationSettingsRepository) {
        this.eventRepository = eventRepository;
        this.presentationSettingsRepository = presentationSettingsRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        boolean developer = hasRole(authentication, "ROLE_DEVELOPER");
        boolean admin = hasRole(authentication, "ROLE_ADMIN");
        boolean organizer = hasRole(authentication, "ROLE_ORGANIZER");
        boolean client = hasRole(authentication, "ROLE_CLIENT");
        boolean designer = hasRole(authentication, "ROLE_DESIGNER");
        boolean gateAgent = hasRole(authentication, "ROLE_GATE_AGENT");

        if (developer) return "redirect:/dashboard/developer";
        if (admin) return "redirect:/dashboard/admin";
        if (organizer) return "redirect:/dashboard/organizer";
        if (client) return "redirect:/dashboard/client";
        if (designer) return "redirect:/dashboard/designer";
        if (gateAgent) return "redirect:/dashboard/gate-agent";
        return "redirect:/";
    }

    @GetMapping("/dashboard/developer")
    public String developer(Model model, Authentication authentication) {
        model.addAttribute("title", "Panel Developer");
        model.addAttribute("email", authentication.getName());
        return "dashboard/developer";
    }

    @GetMapping("/dashboard/admin")
    public String admin(Model model, Authentication authentication) {
        model.addAttribute("title", "Panel Administrador");
        model.addAttribute("email", authentication.getName());
        return "dashboard/admin";
    }

    @GetMapping("/dashboard/organizer")
    public String organizer(Model model, Authentication authentication) {
        model.addAttribute("title", "Panel Organizador");
        model.addAttribute("email", authentication.getName());
        return "dashboard/organizer";
    }

    @GetMapping("/dashboard/client")
    public String client(Model model, Authentication authentication) {
        model.addAttribute("title", "Panel Cliente");
        model.addAttribute("email", authentication.getName());
        return "dashboard/client";
    }

    @GetMapping("/dashboard/designer")
    public String designer(@RequestParam(required = false) Long eventId, Model model, Authentication authentication) {
        List<Event> designEvents = eventRepository.findAll().stream()
                .filter(Event::isDesignEnabled)
                .filter(event -> event.getStatus() != EventStatus.APPROVED
                        && event.getStatus() != EventStatus.PUBLISHED
                        && event.getStatus() != EventStatus.REJECTED
                        && event.getStatus() != EventStatus.CANCELLED)
                .sorted(Comparator.comparing(Event::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        Event selectedEvent = designEvents.stream()
                .filter(event -> eventId != null && eventId.equals(event.getId()))
                .findFirst()
                .orElse(designEvents.isEmpty() ? null : designEvents.get(0));

        EventPresentationSettings presentation = selectedEvent == null ? null
                : presentationSettingsRepository.findByEventId(selectedEvent.getId()).orElse(null);

        model.addAttribute("title", "Estudio de Presentación");
        model.addAttribute("email", authentication.getName());
        model.addAttribute("designEvents", designEvents);
        model.addAttribute("selectedEvent", selectedEvent);
        model.addAttribute("presentation", presentation);
        model.addAttribute("templates", EventDesignTemplate.values());
        return "dashboard/designer";
    }

    @GetMapping("/dashboard/gate-agent")
    public String gateAgent(Model model, Authentication authentication) {
        model.addAttribute("title", "Panel Auxiliar de Puerta");
        model.addAttribute("email", authentication.getName());
        return "dashboard/gate-agent";
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(role));
    }
}
