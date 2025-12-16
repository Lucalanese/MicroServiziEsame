package it.unimol.exam_management_service.dto;

import it.unimol.exam_management_service.enums.GradeStatus;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public class GradeDTO {
    private Long id;

    @NotNull(message = "L'ID dell'iscrizione è obbligatorio")
    private Long enrollmentId;

    private Long examId;

    private String examName;

    private Long studentId;

    private String studentName;

    @Min(value = 0, message = "Il voto non può essere negativo")
    @Max(value = 30, message = "Il voto non può superare 30")
    private Integer grade;

    private Boolean honors;

    private GradeStatus status;

    private LocalDateTime evaluationDate;

    @NotNull(message = "L'ID del professore è obbligatorio")
    private Long professorId;

    private String professorName;

    @Size(max = 1000, message = "Il feedback non può superare i 1000 caratteri")
    private String feedback;

    private LocalDateTime publishedDate;

    private String formattedGrade;

    // Costruttori, getters e setters
    public GradeDTO() {}

    // Getters e setters completi
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(Long enrollmentId) {
        this.enrollmentId = enrollmentId;
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

    public GradeStatus getStatus() {
        return status;
    }

    public void setStatus(GradeStatus status) {
        this.status = status;
    }

    public LocalDateTime getEvaluationDate() {
        return evaluationDate;
    }

    public void setEvaluationDate(LocalDateTime evaluationDate) {
        this.evaluationDate = evaluationDate;
    }

    public Long getProfessorId() {
        return professorId;
    }

    public void setProfessorId(Long professorId) {
        this.professorId = professorId;
    }

    public String getProfessorName() {
        return professorName;
    }

    public void setProfessorName(String professorName) {
        this.professorName = professorName;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public LocalDateTime getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(LocalDateTime publishedDate) {
        this.publishedDate = publishedDate;
    }

    public String getFormattedGrade() {
        return formattedGrade;
    }

    public void setFormattedGrade(String formattedGrade) {
        this.formattedGrade = formattedGrade;
    }
}