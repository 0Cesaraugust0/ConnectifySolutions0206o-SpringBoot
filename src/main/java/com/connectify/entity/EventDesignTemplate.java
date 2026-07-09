package com.connectify.entity;

public enum EventDesignTemplate {
    CLASSIC("Clásico", "Imagen superior, información clara y entradas visibles; sobrio y equilibrado"),
    PROMO_DETAIL("Promocional / Hero", "Banner dominante, título fuerte y foco para festivales, campañas o lanzamientos"),
    POSTER_LANDING("Landing publicitaria", "Portada completa, imagen de campaña y llamado de compra para shows o experiencias visuales"),
    TICKET_PANEL("Venta con panel lateral", "Imagen principal con entradas destacadas para eventos deportivos o de alta demanda"),
    STAGE_MEDIA("Escenario + media", "Presentación con escenario, auspiciadores, video y playlist para ferias o festivales"),
    TOUR_SESSIONS("Gira por sesiones", "Fechas o ciudades desplegables con acceso rápido a compra para recorridos nacionales"),
    IMMERSIVE("Editorial / Premium", "Imagen dominante y composición elegante para galas, cultura o eventos corporativos"),
    CARD_GRID("Compacto / Comercial", "Lectura rápida, datos esenciales y orientación a conversión de venta");

    private final String label;
    private final String description;

    EventDesignTemplate(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }
}
