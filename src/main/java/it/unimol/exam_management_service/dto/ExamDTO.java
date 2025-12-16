package it.unimol.exam_management_service.dto;

import it.unimol.exam_management_service.enums.ExamStatus;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;

public class ExamDTO {
    private Long id;

    @NotBlank(message = "Il nome dell'esame è obbligatorio")
    private String name;

    @NotNull(message = "La data dell'esame è obbligatoria")
    @Future(message = "La data dell'esame deve essere futura")
    private LocalDate date;

    @NotNull(message = "L'orario dell'esame è obbligatorio")
    private LocalTime time;

    @NotNull(message = "L'ID del corso è obbligatorio")
    private Long courseId;

    private String courseName;

    @NotNull(message = "L'ID del professore è obbligatorio")
    private Long professorId;

    private String professorName;

    @NotNull(message = "L'ID dell'aula è obbligatorio")
    private Long classroomId;

    private String classroomName;

    @NotNull(message = "Il numero massimo di studenti è obbligatorio")
    @Min(value = 1, message = "Il numero massimo di studenti deve essere almeno 1")
    @Max(value = 500, message = "Il numero massimo di studenti non può superare 500")
    private Integer maxStudents;

    @NotNull(message = "La scadenza per le iscrizioni è obbligatoria")
    private LocalDate enrollmentDeadline;

    private ExamStatus status;
    private String notes;
    private Integer currentEnrollments;
    private Boolean enrollmentOpen;

    // Costruttori, getters e setters
    public ExamDTO() {}

    // Getter e setter (tutti completi)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
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

    public Long getClassroomId() {
        return classroomId;
    }

    public void setClassroomId(Long classroomId) {
        this.classroomId = classroomId;
    }

    public String getClassroomName() {
        return classroomName;
    }

    public void setClassroomName(String classroomName) {
        this.classroomName = classroomName;
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

    public Integer getCurrentEnrollments() {
        return currentEnrollments;
    }

    public void setCurrentEnrollments(Integer currentEnrollments) {
        this.currentEnrollments = currentEnrollments;
    }

    public Boolean getEnrollmentOpen() {
        return enrollmentOpen;
    }

    public void setEnrollmentOpen(Boolean enrollmentOpen) {
        this.enrollmentOpen = enrollmentOpen;
    }
}