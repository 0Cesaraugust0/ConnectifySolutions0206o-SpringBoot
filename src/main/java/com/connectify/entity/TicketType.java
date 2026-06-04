package com.connectify.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_types")
public class TicketType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @Column(nullable = false)
    private String name;

    @Column(length = 800)
    private String description;

    @Column(nullable = false)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer quantityAvailable = 0;

    @Column(nullable = false)
    private Integer quantitySold = 0;

    private LocalDateTime salesStart;

    private LocalDateTime salesEnd;

    private boolean active = true;

    public Long getId() { return id; }
    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getQuantityAvailable() { return quantityAvailable; }
    public void setQuantityAvailable(Integer quantityAvailable) { this.quantityAvailable = quantityAvailable; }
    public Integer getQuantitySold() { return quantitySold; }
    public void setQuantitySold(Integer quantitySold) { this.quantitySold = quantitySold; }
    public LocalDateTime getSalesStart() { return salesStart; }
    public void setSalesStart(LocalDateTime salesStart) { this.salesStart = salesStart; }
    public LocalDateTime getSalesEnd() { return salesEnd; }
    public void setSalesEnd(LocalDateTime salesEnd) { this.salesEnd = salesEnd; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Integer getRemaining() {
        int available = quantityAvailable == null ? 0 : quantityAvailable;
        int sold = quantitySold == null ? 0 : quantitySold;
        return Math.max(available - sold, 0);
    }
}
