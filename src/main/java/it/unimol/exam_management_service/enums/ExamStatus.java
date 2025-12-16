package it.unimol.exam_management_service.enums;

public enum ExamStatus {
    SCHEDULED("Programmato"),
    ENROLLMENT_OPEN("Iscrizioni aperte"),
    ENROLLMENT_CLOSED("Iscrizioni chiuse"),
    ONGOING("In corso"),
    COMPLETED("Completato"),
    CANCELLED("Annullato");

    private final String description;

    ExamStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}