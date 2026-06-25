package com.connectify.controller;

import com.connectify.entity.Event;
import com.connectify.entity.EventStatus;
import com.connectify.entity.Purchase;
import com.connectify.entity.TicketType;
import com.connectify.repository.TicketTypeRepository;
import com.connectify.service.CartService;
import com.connectify.service.EventService;
import com.connectify.service.PurchaseService;
import jakarta.servlet.http.HttpSession;
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

    public CartController(CartService cartService, EventService eventService, PurchaseService purchaseService,
                          TicketTypeRepository ticketTypeRepository) {
        this.cartService = cartService;
        this.eventService = eventService;
        this.purchaseService = purchaseService;
        this.ticketTypeRepository = ticketTypeRepository;
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

        cartService.addEvent(session, event, ticketType, quantity);
        return "redirect:/cart";
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
    public String checkout(HttpSession session, Model model) {
        addCartAttributes(session, model);
        return "cart/checkout";
    }

    @PostMapping("/checkout/confirm")
    public String confirmCheckout(@RequestParam String firstName,
                                  @RequestParam String lastName,
                                  @RequestParam String dni,
                                  @RequestParam String phone,
                                  @RequestParam String email,
                                  HttpSession session) {
        Purchase purchase = purchaseService.completePurchase(session, firstName, lastName, dni, phone, email);
        return "redirect:/cart/confirmation/" + purchase.getId();
    }

    @GetMapping("/confirmation/{purchaseId}")
    public String confirmation(@PathVariable Long purchaseId, Model model) {
        model.addAttribute("purchase", purchaseService.findById(purchaseId));
        return "cart/confirmation";
    }

    private void addCartAttributes(HttpSession session, Model model) {
        model.addAttribute("items", cartService.getItems(session));
        model.addAttribute("cartCount", cartService.countItems(session));
        model.addAttribute("subtotal", cartService.subtotal(session));
        model.addAttribute("serviceFee", cartService.serviceFee(session));
        model.addAttribute("total", cartService.total(session));
    }
}
