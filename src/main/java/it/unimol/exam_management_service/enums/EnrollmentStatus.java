package it.unimol.exam_management_service.enums;

public enum EnrollmentStatus {
    ENROLLED("Iscritto"),
    PRESENT("Presente"),
    ABSENT("Assente"),
    WITHDREW("Ritirato"),
    GRADED("Valutato");

    private final String description;

    EnrollmentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}