package com.connectify.entity;

public enum EventDesignTemplate {
    CLASSIC("Clásico", "Imagen superior, información clara y entradas visibles; sobrio y equilibrado"),
    PROMO_DETAIL("Promocional / Hero", "Banner dominante, título fuerte y foco para festivales, campañas o lanzamientos"),
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
