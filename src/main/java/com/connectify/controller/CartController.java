package com.connectify.controller;

import com.connectify.entity.Event;
import com.connectify.entity.Purchase;
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

    public CartController(CartService cartService, EventService eventService, PurchaseService purchaseService) {
        this.cartService = cartService;
        this.eventService = eventService;
        this.purchaseService = purchaseService;
    }

    @GetMapping
    public String cart(HttpSession session, Model model) {
        addCartAttributes(session, model);
        return "cart/index";
    }

    @PostMapping("/add/{eventId}")
    public String add(@PathVariable Long eventId,
                      @RequestParam(defaultValue = "1") int quantity,
                      HttpSession session) {
        Event event = eventService.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Evento no encontrado"));
        cartService.addEvent(session, event, quantity);
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
