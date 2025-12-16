package it.unimol.exam_management_service.enums;

public enum GradeStatus {
    NOT_EVALUATED("Non valutato"),
    EVALUATED("Valutato"),
    PUBLISHED("Pubblicato"),
    REJECTED("Rifiutato");

    private final String description;

    GradeStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}