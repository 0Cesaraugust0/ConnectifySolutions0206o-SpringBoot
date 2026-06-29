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

    @Column(length = 1000)
    private String coverImageUrl;

    @Column(length = 1000)
    private String thumbnailImageUrl;

    @Column(length = 20)
    private String coverFocus = "CENTER";

    @Column(length = 20)
    private String contentFocus = "BALANCED";

    @Column(length = 20)
    private String infoPosition = "STANDARD";

    private boolean showGallery = true;

    private boolean showOrganizer = true;

    private boolean showAgenda = false;

    @Column(length = 1500)
    private String agendaText;

    private boolean showBenefits = false;

    @Column(length = 1500)
    private String benefitsText;

    private boolean showSponsors = false;

    @Column(length = 1200)
    private String sponsorsText;

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
    public String getCoverImageUrl() { return coverImageUrl; }
    public void setCoverImageUrl(String coverImageUrl) { this.coverImageUrl = coverImageUrl; touch(); }
    public String getThumbnailImageUrl() { return thumbnailImageUrl; }
    public void setThumbnailImageUrl(String thumbnailImageUrl) { this.thumbnailImageUrl = thumbnailImageUrl; touch(); }
    public String getCoverFocus() { return coverFocus; }
    public void setCoverFocus(String coverFocus) { this.coverFocus = coverFocus; touch(); }
    public String getContentFocus() { return contentFocus; }
    public void setContentFocus(String contentFocus) { this.contentFocus = contentFocus; touch(); }
    public String getInfoPosition() { return infoPosition; }
    public void setInfoPosition(String infoPosition) { this.infoPosition = infoPosition; touch(); }
    public boolean isShowGallery() { return showGallery; }
    public void setShowGallery(boolean showGallery) { this.showGallery = showGallery; touch(); }
    public boolean isShowOrganizer() { return showOrganizer; }
    public void setShowOrganizer(boolean showOrganizer) { this.showOrganizer = showOrganizer; touch(); }
    public boolean isShowAgenda() { return showAgenda; }
    public void setShowAgenda(boolean showAgenda) { this.showAgenda = showAgenda; touch(); }
    public String getAgendaText() { return agendaText; }
    public void setAgendaText(String agendaText) { this.agendaText = agendaText; touch(); }
    public boolean isShowBenefits() { return showBenefits; }
    public void setShowBenefits(boolean showBenefits) { this.showBenefits = showBenefits; touch(); }
    public String getBenefitsText() { return benefitsText; }
    public void setBenefitsText(String benefitsText) { this.benefitsText = benefitsText; touch(); }
    public boolean isShowSponsors() { return showSponsors; }
    public void setShowSponsors(boolean showSponsors) { this.showSponsors = showSponsors; touch(); }
    public String getSponsorsText() { return sponsorsText; }
    public void setSponsorsText(String sponsorsText) { this.sponsorsText = sponsorsText; touch(); }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    private void touch() { this.updatedAt = LocalDateTime.now(); }
}
