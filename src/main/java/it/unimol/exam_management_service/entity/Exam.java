package it.unimol.exam_management_service.entity;

import it.unimol.exam_management_service.enums.ExamStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "exams")
public class Exam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Il nome dell'esame è obbligatorio")
    @Size(max = 255, message = "Il nome dell'esame non può superare i 255 caratteri")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "La data dell'esame è obbligatoria")
    @Future(message = "La data dell'esame deve essere futura")
    @Column(nullable = false)
    private LocalDate date;

    @NotNull(message = "L'orario dell'esame è obbligatorio")
    @Column(nullable = false)
    private LocalTime time;

    @NotNull(message = "Il corso è obbligatorio")
    @Column(name = "course_id", nullable = false)
    private Long courseId;

    @NotNull(message = "Il professore è obbligatorio")
    @Column(name = "professor_id", nullable = false)
    private Long professorId;

    @NotNull(message = "L'aula è obbligatoria")
    @Column(name = "classroom_id", nullable = false)
    private Long classroomId;

    @NotNull(message = "Il numero massimo di studenti è obbligatorio")
    @Min(value = 1, message = "Il numero massimo di studenti deve essere almeno 1")
    @Max(value = 500, message = "Il numero massimo di studenti non può superare 500")
    @Column(name = "max_students", nullable = false)
    private Integer maxStudents;

    @NotNull(message = "La scadenza per le iscrizioni è obbligatoria")
    @Column(name = "enrollment_deadline", nullable = false)
    private LocalDate enrollmentDeadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExamStatus status = ExamStatus.SCHEDULED;

    @Size(max = 1000, message = "Le note non possono superare i 1000 caratteri")
    @Column(length = 1000)
    private String notes;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExamEnrollment> enrollments;

    // Costruttori
    public Exam() {
    }

    public Exam(String name, LocalDate date, LocalTime time, Long courseId, Long professorId,
                Long classroomId, Integer maxStudents, LocalDate enrollmentDeadline) {
        this.name = name;
        this.date = date;
        this.time = time;
        this.courseId = courseId;
        this.professorId = professorId;
        this.classroomId = classroomId;
        this.maxStudents = maxStudents;
        this.enrollmentDeadline = enrollmentDeadline;
        this.status = ExamStatus.SCHEDULED;
    }

    // Getter e Setter
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Long getProfessorId() {
        return professorId;
    }

    public void setProfessorId(Long professorId) {
        this.professorId = professorId;
    }

    public Long getClassroomId() {
        return classroomId;
    }

    public void setClassroomId(Long classroomId) {
        this.classroomId = classroomId;
    }

    public Integer getMaxStudents() {
        return maxStudents;
    }

    public void setMaxStudents(Integer maxStudents) {
        this.maxStudents = maxStudents;
    }

    public LocalDate getEnrollmentDeadline() {
        return enrollmentDeadline;
    }

    public void setEnrollmentDeadline(LocalDate enrollmentDeadline) {
        this.enrollmentDeadline = enrollmentDeadline;
    }

    public ExamStatus getStatus() {
        return status;
    }

    public void setStatus(ExamStatus status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<ExamEnrollment> getEnrollments() {
        return enrollments;
    }

    public void setEnrollments(List<ExamEnrollment> enrollments) {
        this.enrollments = enrollments;
    }

    // Metodi di utilità
    public int getCurrentEnrollmentCount() {
        return enrollments != null ? enrollments.size() : 0;
    }

    public boolean isEnrollmentOpen() {
        return LocalDate.now().isBefore(enrollmentDeadline) &&
                status == ExamStatus.SCHEDULED &&
                getCurrentEnrollmentCount() < maxStudents;
    }

    public boolean hasAvailableSpots() {
        return getCurrentEnrollmentCount() < maxStudents;
    }

}