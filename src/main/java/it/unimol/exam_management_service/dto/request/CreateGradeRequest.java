package it.unimol.exam_management_service.dto.request;

import jakarta.validation.constraints.*;

public class CreateGradeRequest {
    @NotNull(message = "L'ID dell'iscrizione è obbligatorio")
    private Long enrollmentId;

    @Min(value = 0, message = "Il voto non può essere negativo")
    @Max(value = 30, message = "Il voto non può superare 30")
    private Integer grade;

    private Boolean honors = false;

    @NotNull(message = "L'ID del professore è obbligatorio")
    private Long professorId;

    @Size(max = 1000, message = "Il feedback non può superare i 1000 caratteri")
    private String feedback;

    // Getters e setters
    public Long getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(Long enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public Boolean getHonors() {
        return honors;
    }

    public void setHonors(Boolean honors) {
        this.honors = honors;
    }

    public Long getProfessorId() {
        return professorId;
    }

    public void setProfessorId(Long professorId) {
        this.professorId = professorId;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }
}