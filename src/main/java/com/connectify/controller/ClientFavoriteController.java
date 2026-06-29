package com.connectify.controller;

import com.connectify.entity.ClientFavorite;
import com.connectify.entity.Event;
import com.connectify.entity.EventStatus;
import com.connectify.entity.UserAccount;
import com.connectify.repository.ClientFavoriteRepository;
import com.connectify.repository.UserAccountRepository;
import com.connectify.service.EventService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/dashboard/client/favorites")
public class ClientFavoriteController {

    private final ClientFavoriteRepository favoriteRepository;
    private final UserAccountRepository userAccountRepository;
    private final EventService eventService;

    public ClientFavoriteController(ClientFavoriteRepository favoriteRepository,
                                    UserAccountRepository userAccountRepository,
                                    EventService eventService) {
        this.favoriteRepository = favoriteRepository;
        this.userAccountRepository = userAccountRepository;
        this.eventService = eventService;
    }

    @GetMapping
    public String favorites(Model model, Authentication authentication) {
        UserAccount account = currentClient(authentication);
        List<ClientFavorite> favorites = favoriteRepository.findByClient_EmailIgnoreCaseOrderByCreatedAtDesc(account.getEmail());
        model.addAttribute("favorites", favorites);
        model.addAttribute("email", account.getEmail());
        model.addAttribute("firstName", firstName(account.getFullName()));
        model.addAttribute("favoriteCount", favoriteRepository.countByClient_Id(account.getId()));
        return "dashboard/client/favorites";
    }

    @PostMapping("/toggle/{eventId}")
    public String toggle(@PathVariable Long eventId,
                         @RequestParam(defaultValue = "/events") String redirectTo,
                         Authentication authentication) {
        UserAccount account = currentClient(authentication);
        Event event = eventService.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
        if (event.getStatus() != EventStatus.PUBLISHED) {
            return redirect(redirectTo);
        }

        favoriteRepository.findByClient_IdAndEvent_Id(account.getId(), eventId)
                .ifPresentOrElse(favoriteRepository::delete, () -> {
                    ClientFavorite favorite = new ClientFavorite();
                    favorite.setClient(account);
                    favorite.setEvent(event);
                    favoriteRepository.save(favorite);
                });
        return redirect(redirectTo);
    }

    private UserAccount currentClient(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new AccessDeniedException("Se requiere una cuenta Cliente.");
        }
        return userAccountRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("Cuenta Cliente no encontrada."));
    }

    private String redirect(String path) {
        if (path != null && (path.startsWith("/events") || path.startsWith("/dashboard/client"))) {
            return "redirect:" + path;
        }
        return "redirect:/events";
    }

    private String firstName(String fullName) {
        if (fullName == null || fullName.isBlank()) return "Mi cuenta";
        return fullName.trim().split("\\s+")[0];
    }
}
