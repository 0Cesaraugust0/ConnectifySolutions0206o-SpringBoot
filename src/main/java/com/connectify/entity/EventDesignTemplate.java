package com.connectify.entity;

public enum EventDesignTemplate {
    CLASSIC("Clásico", "Diseño actual limpio y funcional"),
    CARD_GRID("Cards marketplace", "Estilo catálogo de eventos con cards, categoría, fecha y precio desde"),
    PROMO_DETAIL("Detalle promocional", "Landing de evento con hero, datos fuertes y bloque de entradas"),
    IMMERSIVE("Inmersivo premium", "Presentación visual grande para eventos destacados o campañas especiales");

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
