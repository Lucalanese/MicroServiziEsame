package it.unimol.exam_management_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.unimol.exam_management_service.dto.ExamDTO;
import it.unimol.exam_management_service.dto.request.CreateExamRequest;
import it.unimol.exam_management_service.enums.ExamStatus;
import it.unimol.exam_management_service.service.ExamService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/exams")
@Tag(name = "Exams", description = "API per la gestione degli esami")
public class ExamController {

    private final ExamService examService;

    @Autowired
    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    @PostMapping
    //@PreAuthorize("hasRole('DOCENTE') or hasRole('admin')")
    @Operation(summary = "Crea nuovo esame", description = "Crea un nuovo esame nel sistema")
    public ResponseEntity<ExamDTO> createExam(@Valid @RequestBody CreateExamRequest examRequest) {
        ExamDTO createdExam = examService.createExam(examRequest);
        return new ResponseEntity<>(createdExam, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('STUDENTE', 'DOCENTE', 'admin')")
    @Operation(summary = "Lista tutti gli esami", description = "Ottieni la lista di tutti gli esami con filtri opzionali")
    public ResponseEntity<List<ExamDTO>> getAllExams(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long teacherId,
            @PageableDefault(size = 20) Pageable pageable) {

        List<ExamDTO> exams = examService.getAllExams(startDate, endDate, courseId, teacherId, pageable);
        return ResponseEntity.ok(exams);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('STUDENTE', 'DOCENTE', 'admin')")
    @Operation(summary = "Dettaglio singolo esame", description = "Ottieni i dettagli di un esame specifico")
    public ResponseEntity<ExamDTO> getExamById(@PathVariable Long id) {
        ExamDTO exam = examService.getExamById(id);
        return ResponseEntity.ok(exam);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCENTE') or hasRole('admin')")
    @Operation(summary = "Aggiorna esame", description = "Aggiorna i dettagli di un esame esistente")
    public ResponseEntity<ExamDTO> updateExam(@PathVariable Long id, @Valid @RequestBody ExamDTO examDTO) {
        ExamDTO updatedExam = examService.updateExam(id, examDTO);
        return ResponseEntity.ok(updatedExam);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "Elimina esame", description = "Elimina un esame dal sistema")
    public ResponseEntity<Void> deleteExam(@PathVariable Long id) {
        examService.deleteExam(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('STUDENTE', 'DOCENTE', 'admin')")
    @Operation(summary = "Esami per corso", description = "Ottieni la lista degli esami per un corso specifico")
    public ResponseEntity<List<ExamDTO>> getExamsByCourse(@PathVariable Long courseId) {
        List<ExamDTO> exams = examService.getExamsByCourse(courseId);
        return ResponseEntity.ok(exams);
    }

    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("hasAnyRole('STUDENTE', 'DOCENTE', 'admin')")
    @Operation(summary = "Esami per docente", description = "Ottieni la lista degli esami per un docente specifico")
    public ResponseEntity<List<ExamDTO>> getExamsByTeacher(@PathVariable Long teacherId) {
        List<ExamDTO> exams = examService.getExamsByProfessor(teacherId);
        return ResponseEntity.ok(exams);
    }

    @GetMapping("/calendar")
    // Accessibile pubblicamente - non necessita di annotazione PreAuthorize
    @Operation(summary = "Calendario esami pubblico", description = "Ottieni il calendario degli esami con filtri opzionali")
    public ResponseEntity<List<ExamDTO>> getExamCalendar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long teacherId) {

        List<ExamDTO> calendarExams = examService.getExamCalendar(startDate, endDate, courseId, teacherId);
        return ResponseEntity.ok(calendarExams);
    }

    @GetMapping("/available")
    @PreAuthorize("hasRole('STUDENTE')")
    @Operation(summary = "Esami disponibili per iscrizione", description = "Ottieni la lista degli esami disponibili per l'iscrizione")
    public ResponseEntity<List<ExamDTO>> getAvailableExams(
            @RequestParam(required = false) Long studentId) {

        List<ExamDTO> availableExams = examService.getAvailableExams(studentId);
        return ResponseEntity.ok(availableExams);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('DOCENTE') or hasRole('admin')")
    @Operation(summary = "Aggiorna stato esame", description = "Aggiorna lo stato di un esame")
    public ResponseEntity<Void> updateExamStatus(
            @PathVariable Long id,
            @RequestParam ExamStatus status) {

        examService.updateExamStatus(id, status);
        return ResponseEntity.noContent().build();
    }
}