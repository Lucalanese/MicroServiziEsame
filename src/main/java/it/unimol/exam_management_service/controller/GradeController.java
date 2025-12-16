package it.unimol.exam_management_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.unimol.exam_management_service.dto.GradeDTO;
import it.unimol.exam_management_service.dto.request.CreateGradeRequest;
import it.unimol.exam_management_service.service.GradeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Grades", description = "API per la gestione dei voti degli esami")
public class GradeController {

    private final GradeService gradeService;

    @Autowired
    public GradeController(GradeService gradeService) {
        this.gradeService = gradeService;
    }

    @PostMapping("/exams/{examId}/grades")
    @PreAuthorize("hasRole('DOCENTE')")
    @Operation(summary = "Registra voto", description = "Registra un voto per un'iscrizione a un esame")
    public ResponseEntity<GradeDTO> recordGrade(
            @PathVariable Long examId,
            @Valid @RequestBody CreateGradeRequest request) {

        GradeDTO grade = gradeService.recordGrade(examId, request);
        return new ResponseEntity<>(grade, HttpStatus.CREATED);
    }

    @GetMapping("/exams/{examId}/grades")
    @PreAuthorize("hasRole('DOCENTE') or hasRole('admin')")
    @Operation(summary = "Voti per esame", description = "Ottieni la lista dei voti per un esame specifico")
    public ResponseEntity<List<GradeDTO>> getExamGrades(
            @PathVariable Long examId,
            @RequestParam(required = false) Integer minGrade,
            @RequestParam(required = false) Integer maxGrade,
            @RequestParam(required = false) Boolean withHonors,
            @PageableDefault(size = 20) Pageable pageable) {

        List<GradeDTO> grades = gradeService.getExamGrades(examId, minGrade, maxGrade, withHonors, pageable);
        return ResponseEntity.ok(grades);
    }

    @GetMapping("/grades/{id}")
    @PreAuthorize("hasAnyRole('STUDENTE', 'DOCENTE', 'admin')")
    @Operation(summary = "Dettaglio voto", description = "Ottieni i dettagli di un voto specifico")
    public ResponseEntity<GradeDTO> getGradeById(@PathVariable Long id) {
        GradeDTO grade = gradeService.getGradeById(id);
        return ResponseEntity.ok(grade);
    }

    @PutMapping("/grades/{id}")
    @PreAuthorize("hasRole('DOCENTE')")
    @Operation(summary = "Aggiorna voto", description = "Aggiorna i dettagli di un voto esistente")
    public ResponseEntity<GradeDTO> updateGrade(
            @PathVariable Long id,
            @Valid @RequestBody GradeDTO gradeDTO) {

        GradeDTO updatedGrade = gradeService.updateGrade(id, gradeDTO);
        return ResponseEntity.ok(updatedGrade);
    }

    @DeleteMapping("/grades/{id}")
    @PreAuthorize("hasRole('DOCENTE') or hasRole('admin')")
    @Operation(summary = "Elimina voto", description = "Elimina un voto dal sistema")
    public ResponseEntity<Void> deleteGrade(@PathVariable Long id) {
        gradeService.deleteGrade(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/grades/my")
    @PreAuthorize("hasRole('STUDENTE')")
    @Operation(summary = "I miei voti", description = "Ottieni la lista dei voti di uno studente")
    public ResponseEntity<List<GradeDTO>> getMyGrades(
            @RequestParam Long studentId,
            @RequestParam(required = false) Long courseId) {

        List<GradeDTO> grades = gradeService.getMyGrades(studentId, courseId);
        return ResponseEntity.ok(grades);
    }

    @GetMapping("/grades/student/{studentId}")
    @PreAuthorize("hasRole('DOCENTE') or hasRole('admin')")
    @Operation(summary = "Voti per studente", description = "Ottieni la lista dei voti per uno studente specifico")
    public ResponseEntity<List<GradeDTO>> getStudentGrades(
            @PathVariable Long studentId,
            @RequestParam(required = false) Long courseId) {

        List<GradeDTO> grades = gradeService.getStudentGrades(studentId, courseId);
        return ResponseEntity.ok(grades);
    }

    @PutMapping("/grades/{id}/publish")
    @PreAuthorize("hasRole('DOCENTE')")
    @Operation(summary = "Pubblica voto", description = "Pubblica un voto rendendolo visibile allo studente")
    public ResponseEntity<GradeDTO> publishGrade(@PathVariable Long id) {
        GradeDTO publishedGrade = gradeService.publishGrade(id);
        return ResponseEntity.ok(publishedGrade);
    }

    @PutMapping("/exams/{examId}/grades/publish-all")
    @PreAuthorize("hasRole('DOCENTE')")
    @Operation(summary = "Pubblica tutti i voti", description = "Pubblica tutti i voti di un esame")
    public ResponseEntity<Void> publishAllGrades(@PathVariable Long examId) {
        gradeService.publishAllGradesForExam(examId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/grades/course/{courseId}/statistics")
    @PreAuthorize("hasRole('DOCENTE') or hasRole('admin')")
    @Operation(summary = "Statistiche voti corso", description = "Ottieni le statistiche dei voti per un corso specifico")
    public ResponseEntity<Map<String, Object>> getCourseGradeStatistics(@PathVariable Long courseId) {
        Map<String, Object> statistics = gradeService.getCourseGradeStatistics(courseId);
        return ResponseEntity.ok(statistics);
    }
}