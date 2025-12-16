package it.unimol.exam_management_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.unimol.exam_management_service.dto.EnrollmentDTO;
import it.unimol.exam_management_service.dto.request.EnrollmentRequest;
import it.unimol.exam_management_service.enums.EnrollmentStatus;
import it.unimol.exam_management_service.service.EnrollmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Enrollments", description = "API per la gestione delle iscrizioni agli esami")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @Autowired
    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping("/exams/{examId}/enroll")
    @PreAuthorize("hasRole('STUDENTE')")
    @Operation(summary = "Iscrizione a esame", description = "Iscrivi uno studente a un esame")
    public ResponseEntity<EnrollmentDTO> enrollToExam(
            @PathVariable Long examId,
            @Valid @RequestBody EnrollmentRequest request) {

        EnrollmentDTO enrollment = enrollmentService.enrollToExam(examId, request);
        return new ResponseEntity<>(enrollment, HttpStatus.CREATED);
    }

    @GetMapping("/enrollments/my")
    @PreAuthorize("hasRole('STUDENTE')")
    @Operation(summary = "Le mie iscrizioni", description = "Ottieni la lista delle iscrizioni di uno studente")
    public ResponseEntity<List<EnrollmentDTO>> getMyEnrollments(
            @RequestParam Long studentId,
            @RequestParam(required = false) EnrollmentStatus status) {

        List<EnrollmentDTO> enrollments = enrollmentService.getMyEnrollments(studentId, status);
        return ResponseEntity.ok(enrollments);
    }

    @GetMapping("/enrollments/{id}")
    @PreAuthorize("hasAnyRole('STUDENTE', 'DOCENTE', 'admin')")
    @Operation(summary = "Dettaglio iscrizione", description = "Ottieni i dettagli di un'iscrizione specifica")
    public ResponseEntity<EnrollmentDTO> getEnrollmentById(@PathVariable Long id) {
        EnrollmentDTO enrollment = enrollmentService.getEnrollmentById(id);
        return ResponseEntity.ok(enrollment);
    }

    @DeleteMapping("/enrollments/{id}")
    @PreAuthorize("hasRole('STUDENTE') or hasRole('admin')")
    @Operation(summary = "Cancella iscrizione", description = "Cancella un'iscrizione a un esame")
    public ResponseEntity<Void> cancelEnrollment(
            @PathVariable Long id,
            @RequestParam Long studentId) {

        enrollmentService.cancelEnrollment(id, studentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exams/{examId}/enrollments")
    @PreAuthorize("hasRole('DOCENTE') or hasRole('admin')")
    @Operation(summary = "Iscrizioni per esame", description = "Ottieni la lista delle iscrizioni per un esame specifico")
    public ResponseEntity<List<EnrollmentDTO>> getExamEnrollments(
            @PathVariable Long examId,
            @RequestParam(required = false) EnrollmentStatus status,
            @PageableDefault(size = 20) Pageable pageable) {

        List<EnrollmentDTO> enrollments = enrollmentService.getExamEnrollments(examId, status, pageable);
        return ResponseEntity.ok(enrollments);
    }

    @PutMapping("/enrollments/{enrollmentId}/status")
    @PreAuthorize("hasRole('DOCENTE') or hasRole('admin')")
    @Operation(summary = "Aggiorna stato iscrizione", description = "Aggiorna lo stato di un'iscrizione")
    public ResponseEntity<EnrollmentDTO> updateEnrollmentStatus(
            @PathVariable Long enrollmentId,
            @RequestParam EnrollmentStatus status) {

        EnrollmentDTO updatedEnrollment = enrollmentService.updateEnrollmentStatus(enrollmentId, status);
        return ResponseEntity.ok(updatedEnrollment);
    }

    @GetMapping("/enrollments")
    @PreAuthorize("hasRole('admin')")
    @Operation(summary = "Tutte le iscrizioni", description = "Ottieni la lista di tutte le iscrizioni con filtri opzionali")
    public ResponseEntity<List<EnrollmentDTO>> getAllEnrollments(
            @RequestParam(required = false) Long examId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) EnrollmentStatus status,
            @PageableDefault(size = 20) Pageable pageable) {

        List<EnrollmentDTO> enrollments = enrollmentService.getAllEnrollments(examId, studentId, status, pageable);
        return ResponseEntity.ok(enrollments);
    }

    @GetMapping("/enrollments/student/{studentId}")
    @PreAuthorize("hasRole('DOCENTE') or hasRole('admin')")
    @Operation(summary = "Iscrizioni per studente", description = "Ottieni la lista delle iscrizioni per uno studente specifico")
    public ResponseEntity<List<EnrollmentDTO>> getStudentEnrollments(
            @PathVariable Long studentId,
            @RequestParam(required = false) EnrollmentStatus status) {

        List<EnrollmentDTO> enrollments = enrollmentService.getStudentEnrollments(studentId, status);
        return ResponseEntity.ok(enrollments);
    }
}