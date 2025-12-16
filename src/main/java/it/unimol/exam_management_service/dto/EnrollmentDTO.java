package it.unimol.exam_management_service.dto;

import it.unimol.exam_management_service.enums.EnrollmentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class EnrollmentDTO {
    private Long id;

    @NotNull(message = "L'ID dell'esame è obbligatorio")
    private Long examId;

    private String examName;

    private LocalDate examDate;

    @NotNull(message = "L'ID dello studente è obbligatorio")
    private Long studentId;

    private String studentName;

    private EnrollmentStatus status;

    private LocalDateTime enrollmentDate;

    @Size(max = 500, message = "Le note non possono superare i 500 caratteri")
    private String notes;

    private Boolean hasGrade;

    private Integer grade;

    // Costruttori, getters e setters
    public EnrollmentDTO() {}

    // Getters e setters completi
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getExamId() {
        return examId;
    }

    public void setExamId(Long examId) {
        this.examId = examId;
    }

    public String getExamName() {
        return examName;
    }

    public void setExamName(String examName) {
        this.examName = examName;
    }

    public LocalDate getExamDate() {
        return examDate;
    }

    public void setExamDate(LocalDate examDate) {
        this.examDate = examDate;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public void setStatus(EnrollmentStatus status) {
        this.status = status;
    }

    public LocalDateTime getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(LocalDateTime enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Boolean getHasGrade() {
        return hasGrade;
    }

    public void setHasGrade(Boolean hasGrade) {
        this.hasGrade = hasGrade;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }
}