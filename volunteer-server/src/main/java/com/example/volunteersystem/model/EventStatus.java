package com.example.volunteersystem.model;

public enum EventStatus {
    PLANNED("Планируется"),
    ACTIVE("Активно"),
    COMPLETED("Завершено"),
    CANCELLED("Отменено"),
    DRAFT("Черновик");

    private final String displayName;

    EventStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
