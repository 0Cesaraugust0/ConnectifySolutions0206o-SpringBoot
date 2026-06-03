package com.connectify.service;

import com.connectify.dto.CartItem;
import com.connectify.entity.Event;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {

    private static final String CART_KEY = "CONNECTIFY_CART";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @SuppressWarnings("unchecked")
    public List<CartItem> getItems(HttpSession session) {
        Object value = session.getAttribute(CART_KEY);
        if (value instanceof List<?>) {
            return (List<CartItem>) value;
        }

        List<CartItem> items = new ArrayList<>();
        session.setAttribute(CART_KEY, items);
        return items;
    }

    public void addEvent(HttpSession session, Event event, int quantity) {
        List<CartItem> items = getItems(session);
        int safeQuantity = Math.max(quantity, 1);

        for (CartItem item : items) {
            if (item.getEventId().equals(event.getId())) {
                item.setQuantity(item.getQuantity() + safeQuantity);
                return;
            }
        }

        String date = event.getEventDate() != null ? event.getEventDate().format(FORMATTER) : "Por definir";
        BigDecimal price = event.getPrice() != null ? event.getPrice() : BigDecimal.ZERO;
        items.add(new CartItem(event.getId(), event.getTitle(), date, event.getLocation(), safeQuantity, price));
    }

    public void removeEvent(HttpSession session, Long eventId) {
        getItems(session).removeIf(item -> item.getEventId().equals(eventId));
    }

    public void clear(HttpSession session) {
        getItems(session).clear();
    }

    public int countItems(HttpSession session) {
        return getItems(session).stream().mapToInt(CartItem::getQuantity).sum();
    }

    public BigDecimal subtotal(HttpSession session) {
        return getItems(session).stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal serviceFee(HttpSession session) {
        return subtotal(session).multiply(new BigDecimal("0.05"));
    }

    public BigDecimal total(HttpSession session) {
        return subtotal(session).add(serviceFee(session));
    }
}
