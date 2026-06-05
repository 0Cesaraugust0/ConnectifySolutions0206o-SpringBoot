package com.connectify.entity;

public enum MessageStatus {
    OPEN("Pendiente", "🟡", "Pendiente / En gestión", "pending"),
    IN_PROGRESS("En proceso", "🔵", "Pendiente / En gestión", "pending"),
    REJECTED("Rechazado", "🔴", "Pendiente / En gestión", "pending"),
    OBSERVED("Observado", "🟠", "Pendiente / En gestión", "pending"),
    APPROVED("Aprobado", "🟢", "Resuelto", "resolved"),
    RESOLVED("Resuelto", "🟢", "Resuelto", "resolved"),
    ARCHIVED("Archivado", "⚪", "Archivado", "neutral");

    private final String label;
    private final String icon;
    private final String categoryLabel;
    private final String categoryStyle;

    MessageStatus(String label, String icon, String categoryLabel, String categoryStyle) {
        this.label = label;
        this.icon = icon;
        this.categoryLabel = categoryLabel;
        this.categoryStyle = categoryStyle;
    }

    public String getLabel() {
        return label;
    }

    public String getIcon() {
        return icon;
    }

    public String getCategoryLabel() {
        return categoryLabel;
    }

    public String getCategoryStyle() {
        return categoryStyle;
    }
}
