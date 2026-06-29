package com.connectify.controller;

import com.connectify.entity.Event;
import com.connectify.entity.EventStatus;
import com.connectify.entity.Purchase;
import com.connectify.entity.TicketType;
import com.connectify.entity.UserAccount;
import com.connectify.repository.TicketTypeRepository;
import com.connectify.repository.UserAccountRepository;
import com.connectify.service.CartService;
import com.connectify.service.EventService;
import com.connectify.service.PurchaseService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final EventService eventService;
    private final PurchaseService purchaseService;
    private final TicketTypeRepository ticketTypeRepository;
    private final UserAccountRepository userAccountRepository;

    public CartController(CartService cartService,
                          EventService eventService,
                          PurchaseService purchaseService,
                          TicketTypeRepository ticketTypeRepository,
                          UserAccountRepository userAccountRepository) {
        this.cartService = cartService;
        this.eventService = eventService;
        this.purchaseService = purchaseService;
        this.ticketTypeRepository = ticketTypeRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @GetMapping
    public String cart(HttpSession session, Model model) {
        addCartAttributes(session, model);
        return "cart/index";
    }

    @PostMapping("/add/{eventId}")
    public String add(@PathVariable Long eventId,
                      @RequestParam(defaultValue = "1") int quantity,
                      @RequestParam(required = false) Long ticketTypeId,
                      HttpSession session) {
        return addToCart(eventId, quantity, ticketTypeId, session);
    }

    /**
     * Atajo de Marketplace para el prototipo: agrega una unidad de la variante elegida
     * y lleva al carrito. El checkout sigue siendo la confirmación final de compra.
     */
    @GetMapping("/quick-add/{eventId}")
    public String quickAdd(@PathVariable Long eventId,
                           @RequestParam Long ticketTypeId,
                           HttpSession session) {
        return addToCart(eventId, 1, ticketTypeId, session);
    }

    @PostMapping("/remove/{eventId}")
    public String remove(@PathVariable Long eventId, HttpSession session) {
        cartService.removeEvent(session, eventId);
        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clear(HttpSession session) {
        cartService.clear(session);
        return "redirect:/cart";
    }

    @GetMapping("/checkout")
    public String checkout(HttpSession session, Model model, Authentication authentication) {
        addCartAttributes(session, model);
        UserAccount account = currentAccount(authentication);
        NameParts name = splitName(account.getFullName());
        model.addAttribute("buyerFirstName", name.firstName());
        model.addAttribute("buyerLastName", name.lastName());
        model.addAttribute("buyerEmail", account.getEmail());
        model.addAttribute("buyerDni", text(account.getDni()));
        model.addAttribute("buyerPhone", text(account.getPhone()));
        return "cart/checkout";
    }

    @PostMapping("/checkout/confirm")
    public String confirmCheckout(@RequestParam String firstName,
                                  @RequestParam String lastName,
                                  @RequestParam String dni,
                                  @RequestParam String phone,
                                  HttpSession session,
                                  Authentication authentication) {
        UserAccount account = currentAccount(authentication);
        String normalizedFirstName = firstName == null ? "" : firstName.trim();
        String normalizedLastName = lastName == null ? "" : lastName.trim();
        if (normalizedFirstName.isBlank() || normalizedLastName.isBlank()) {
            return "redirect:/cart/checkout?profileError=true";
        }

        account.setFullName(normalizedFirstName + " " + normalizedLastName);
        account.setDni(text(dni));
        account.setPhone(text(phone));
        userAccountRepository.save(account);

        Purchase purchase = purchaseService.completePurchase(session,
                normalizedFirstName, normalizedLastName, text(dni), text(phone), account.getEmail());
        return "redirect:/cart/confirmation/" + purchase.getId();
    }

    @GetMapping("/confirmation/{purchaseId}")
    public String confirmation(@PathVariable Long purchaseId, Model model, Authentication authentication) {
        Purchase purchase = purchaseService.findById(purchaseId);
        UserAccount account = currentAccount(authentication);
        if (purchase.getBuyerEmail() == null || !purchase.getBuyerEmail().equalsIgnoreCase(account.getEmail())) {
            throw new AccessDeniedException("No puedes ver una compra de otra cuenta.");
        }
        model.addAttribute("purchase", purchase);
        return "cart/confirmation";
    }

    private String addToCart(Long eventId, int quantity, Long ticketTypeId, HttpSession session) {
        Event event = eventService.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
        if (event.getStatus() != EventStatus.PUBLISHED) {
            return "redirect:/events";
        }

        TicketType ticketType = null;
        if (ticketTypeId != null) {
            ticketType = ticketTypeRepository.findById(ticketTypeId)
                    .orElseThrow(() -> new IllegalArgumentException("Tipo de entrada no encontrado"));
            if (!ticketType.isActive() || ticketType.getEvent() == null || !eventId.equals(ticketType.getEvent().getId())) {
                return "redirect:/events/" + eventId;
            }
        }
        cartService.addEvent(session, event, ticketType, Math.max(1, quantity));
        return "redirect:/cart";
    }

    private UserAccount currentAccount(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new AccessDeniedException("Se requiere una cuenta Cliente.");
        }
        return userAccountRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("Cuenta Cliente no encontrada."));
    }

    private NameParts splitName(String fullName) {
        String[] parts = text(fullName).trim().split("\\s+", 2);
        return new NameParts(parts.length > 0 ? parts[0] : "", parts.length > 1 ? parts[1] : "");
    }

    private String text(String value) {
        return value == null ? "" : value.trim();
    }

    private void addCartAttributes(HttpSession session, Model model) {
        model.addAttribute("items", cartService.getItems(session));
        model.addAttribute("cartCount", cartService.countItems(session));
        model.addAttribute("subtotal", cartService.subtotal(session));
        model.addAttribute("serviceFee", cartService.serviceFee(session));
        model.addAttribute("total", cartService.total(session));
    }

    private record NameParts(String firstName, String lastName) {
    }
}
