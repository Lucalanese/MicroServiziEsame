package it.unimol.exam_management_service.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;

public class CreateExamRequest {
    @NotBlank(message = "Il nome dell'esame è obbligatorio")
    @Size(max = 255, message = "Il nome dell'esame non può superare i 255 caratteri")
    private String name;

    @NotNull(message = "La data dell'esame è obbligatoria")
    @Future(message = "La data dell'esame deve essere futura")
    private LocalDate date;

    @NotNull(message = "L'orario dell'esame è obbligatorio")
    private LocalTime time;

    @NotNull(message = "L'ID del corso è obbligatorio")
    private Long courseId;

    @NotNull(message = "L'ID del professore è obbligatorio")
    private Long professorId;

    @NotNull(message = "L'ID dell'aula è obbligatorio")
    private Long classroomId;

    @NotNull(message = "Il numero massimo di studenti è obbligatorio")
    @Min(value = 1, message = "Il numero massimo di studenti deve essere almeno 1")
    @Max(value = 500, message = "Il numero massimo di studenti non può superare 500")
    private Integer maxStudents;

    @NotNull(message = "La scadenza per le iscrizioni è obbligatoria")
    private LocalDate enrollmentDeadline;

    @Size(max = 1000, message = "Le note non possono superare i 1000 caratteri")
    private String notes;

    // Getters e setters
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}