package it.unimol.exam_management_service.entity;

import it.unimol.exam_management_service.enums.EnrollmentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "exam_enrollments")
public class ExamEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "L'ID dello studente è obbligatorio")
    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @NotNull(message = "L'esame è obbligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_id", nullable = false)
    private Exam exam;

    @NotNull(message = "La data di iscrizione è obbligatoria")
    @Column(name = "enrollment_date", nullable = false)
    private LocalDateTime enrollmentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status = EnrollmentStatus.ENROLLED;

    @OneToOne(mappedBy = "enrollment", cascade = CascadeType.ALL, orphanRemoval = true)
    private ExamGrade grade;

    @Column(length = 500)
    private String notes;

    // Costruttori
    public ExamEnrollment() {
        this.enrollmentDate = LocalDateTime.now();
    }

    public ExamEnrollment(Long studentId, Exam exam) {
        this.studentId = studentId;
        this.exam = exam;
        this.enrollmentDate = LocalDateTime.now();
        this.status = EnrollmentStatus.ENROLLED;
    }

    // Getter e Setter
    public Long getId() {
        return id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Exam getExam() {
        return exam;
    }

    public void setExam(Exam exam) {
        this.exam = exam;
    }

    public LocalDateTime getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(LocalDateTime enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public void setStatus(EnrollmentStatus status) {
        this.status = status;
    }

    public ExamGrade getGrade() {
        return grade;
    }

    public void setGrade(ExamGrade grade) {
        this.grade = grade;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Metodi di utilità
    public boolean canWithdraw() {
        return status == EnrollmentStatus.ENROLLED &&
                exam.getEnrollmentDeadline().isAfter(LocalDateTime.now().toLocalDate());
    }


    public boolean hasGrade() {
        return grade != null && grade.getGrade() != null && grade.getGrade() > 0;
    }

    public void withdraw() {
        if (canWithdraw()) {
            this.status = EnrollmentStatus.WITHDREW;
        } else {
            throw new IllegalStateException("Non è possibile ritirarsi dall'esame in questo momento");
        }
    }


}