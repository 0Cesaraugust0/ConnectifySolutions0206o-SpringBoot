package com.connectify.controller;

import com.connectify.entity.Category;
import com.connectify.entity.Event;
import com.connectify.entity.EventStatus;
import com.connectify.repository.CategoryRepository;
import com.connectify.repository.EventRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/prototype/organizer/events")
public class OrganizerPrototypeController {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;

    public OrganizerPrototypeController(EventRepository eventRepository, CategoryRepository categoryRepository) {
        this.eventRepository = eventRepository;
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public String myEvents(Model model) {
        model.addAttribute("events", eventRepository.findAll());
        return "prototype/organizer/events";
    }

    @GetMapping("/new")
    public String newEvent(Model model) {
        model.addAttribute("categories", categoryRepository.findAll());
        return "prototype/organizer/event-form";
    }

    @PostMapping
    public String createEvent(@RequestParam String title,
                              @RequestParam String description,
                              @RequestParam Long categoryId,
                              @RequestParam String eventDate,
                              @RequestParam String location,
                              @RequestParam String city,
                              @RequestParam BigDecimal price,
                              @RequestParam Integer capacity,
                              @RequestParam(required = false) String imageUrl) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada"));

        Event event = new Event();
        event.setTitle(title);
        event.setDescription(description);
        event.setCategory(category);
        event.setEventDate(LocalDateTime.parse(eventDate));
        event.setLocation(location);
        event.setCity(city);
        event.setPrice(price);
        event.setCapacity(capacity);
        event.setSold(0);
        event.setImageUrl(imageUrl == null ? "" : imageUrl);
        event.setFeatured(false);
        event.setStatus(EventStatus.PUBLISHED);
        eventRepository.save(event);

        return "redirect:/prototype/organizer/events";
    }
}
