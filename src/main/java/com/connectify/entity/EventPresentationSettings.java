package com.connectify.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "event_presentation_settings")
public class EventPresentationSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "event_id", nullable = false, unique = true)
    private Event event;

    @Column(length = 20)
    private String primaryColor = "#4f46e5";

    @Column(length = 20)
    private String accentColor = "#8b5cf6";

    @Column(length = 500)
    private String highlightText;

    private boolean showGallery = true;

    private boolean showOrganizer = true;

    private LocalDateTime updatedAt = LocalDateTime.now();

    public Long getId() { return id; }
    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; touch(); }
    public String getPrimaryColor() { return primaryColor; }
    public void setPrimaryColor(String primaryColor) { this.primaryColor = primaryColor; touch(); }
    public String getAccentColor() { return accentColor; }
    public void setAccentColor(String accentColor) { this.accentColor = accentColor; touch(); }
    public String getHighlightText() { return highlightText; }
    public void setHighlightText(String highlightText) { this.highlightText = highlightText; touch(); }
    public boolean isShowGallery() { return showGallery; }
    public void setShowGallery(boolean showGallery) { this.showGallery = showGallery; touch(); }
    public boolean isShowOrganizer() { return showOrganizer; }
    public void setShowOrganizer(boolean showOrganizer) { this.showOrganizer = showOrganizer; touch(); }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    private void touch() { this.updatedAt = LocalDateTime.now(); }
}
