package it.unimol.exam_management_service.entity;

import it.unimol.exam_management_service.enums.GradeStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "exam_grades")
public class ExamGrade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(value = 0, message = "Il voto non può essere negativo")
    @Max(value = 30, message = "Il voto non può superare 30")
    @Column(name = "grade_value")
    private Integer grade; // Cambiato da int a Integer per permettere null

    @Column(name = "has_honors")
    private Boolean honors = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GradeStatus status = GradeStatus.NOT_EVALUATED;

    @NotNull(message = "La data di valutazione è obbligatoria")
    @Column(name = "evaluation_date", nullable = false)
    private LocalDateTime evaluationDate;

    @NotNull(message = "L'ID del professore è obbligatorio")
    @Column(name = "professor_id", nullable = false)
    private Long professorId; // Chi ha assegnato il voto

    @Size(max = 1000, message = "Il feedback non può superare i 1000 caratteri")
    @Column(length = 1000)
    private String feedback;

    @Column(name = "published_date")
    private LocalDateTime publishedDate;

    @NotNull(message = "L'iscrizione è obbligatoria")
    @OneToOne
    @JoinColumn(name = "enrollment_id", nullable = false)
    private ExamEnrollment enrollment;

    // Costruttori
    public ExamGrade() {
        this.evaluationDate = LocalDateTime.now();
    }

    public ExamGrade(Integer grade, Boolean honors, Long professorId, ExamEnrollment enrollment) {
        this.grade = grade;
        this.honors = honors != null ? honors : false;
        this.professorId = professorId;
        this.enrollment = enrollment;
        this.evaluationDate = LocalDateTime.now();
        this.status = grade != null ? GradeStatus.EVALUATED : GradeStatus.NOT_EVALUATED;
    }

    public ExamGrade(Integer grade, Boolean honors, Long professorId, ExamEnrollment enrollment, String feedback) {
        this(grade, honors, professorId, enrollment);
        this.feedback = feedback;
    }

    // Getter e Setter
    public Long getId() {
        return id;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
        // Aggiorna automaticamente lo status quando si imposta un voto
        if (grade != null) {
            this.status = GradeStatus.EVALUATED;
        }
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

    public ExamEnrollment getEnrollment() {
        return enrollment;
    }

    public void setEnrollment(ExamEnrollment enrollment) {
        this.enrollment = enrollment;
    }

    // Metodi di utilità
    public boolean isPassed() {
        return grade != null && grade >= 18;
    }

    public boolean isFailed() {
        return grade != null && grade < 18;
    }

    public boolean isEvaluated() {
        return status == GradeStatus.EVALUATED || status == GradeStatus.PUBLISHED;
    }

    public boolean isPublished() {
        return status == GradeStatus.PUBLISHED && publishedDate != null;
    }

    public String getFormattedGrade() {
        if (grade == null) {
            return "Non valutato";
        }
        if (grade < 18) {
            return "Insufficiente (" + grade + ")";
        }
        if (grade == 30 && Boolean.TRUE.equals(honors)) {
            return "30 e lode";
        }
        return grade.toString();
    }

    public void publish() {
        if (status == GradeStatus.EVALUATED) {
            this.status = GradeStatus.PUBLISHED;
            this.publishedDate = LocalDateTime.now();
        } else {
            throw new IllegalStateException("Il voto deve essere valutato prima di essere pubblicato");
        }
    }

    public void reject() {
        if (status == GradeStatus.EVALUATED || status == GradeStatus.PUBLISHED) {
            this.status = GradeStatus.REJECTED;
            this.publishedDate = LocalDateTime.now();
        } else {
            throw new IllegalStateException("Il voto deve essere valutato prima di essere rifiutato");
        }
    }

    public boolean canBeModified() {
        return status != GradeStatus.PUBLISHED && status != GradeStatus.REJECTED;
    }
}