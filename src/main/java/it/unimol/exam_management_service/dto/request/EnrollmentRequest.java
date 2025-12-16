package it.unimol.exam_management_service.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class EnrollmentRequest {
    @NotNull(message = "L'ID dello studente è obbligatorio")
    private Long studentId;

    @Size(max = 500, message = "Le note non possono superare i 500 caratteri")
    private String notes;

    // Getters e setters
    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}