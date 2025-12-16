package it.unimol.exam_management_service.repository;

import it.unimol.exam_management_service.entity.ExamEnrollment;
import it.unimol.exam_management_service.enums.EnrollmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<ExamEnrollment, Long> {

    // Trova iscrizione per uno studente e un esame specifico
    Optional<ExamEnrollment> findByStudentIdAndExamId(Long studentId, Long examId);

    // Tutte le iscrizioni di uno studente
    List<ExamEnrollment> findByStudentIdOrderByEnrollmentDateDesc(Long studentId);

    // Iscrizioni di uno studente con uno stato specifico
    List<ExamEnrollment> findByStudentIdAndStatusOrderByEnrollmentDateDesc(Long studentId, EnrollmentStatus status);

    // Iscrizioni per un esame specifico
    List<ExamEnrollment> findByExamIdOrderByEnrollmentDateAsc(Long examId);

    // Iscrizioni per un esame con uno stato specifico
    List<ExamEnrollment> findByExamIdAndStatusOrderByEnrollmentDateAsc(Long examId, EnrollmentStatus status);

    // Conteggio iscrizioni per un esame
    long countByExamId(Long examId);

    // Ricerca paginata per esame e stato
    Page<ExamEnrollment> findByExamIdAndStatus(Long examId, EnrollmentStatus status, Pageable pageable);

    // Iscrizioni attive (non ritirate) per uno studente
    @Query("SELECT e FROM ExamEnrollment e WHERE e.studentId = :studentId AND e.status != 'WITHDREW'")
    List<ExamEnrollment> findActiveEnrollmentsByStudent(@Param("studentId") Long studentId);

    // Controlla se uno studente è già iscritto a un esame
    boolean existsByStudentIdAndExamId(Long studentId, Long examId);

    // Nuovi metodi per supportare la paginazione e filtri combinati

    // Trova iscrizioni per esame con paginazione
    Page<ExamEnrollment> findByExamId(Long examId, Pageable pageable);

    // Trova iscrizioni per studente con paginazione
    Page<ExamEnrollment> findByStudentId(Long studentId, Pageable pageable);

    // Trova iscrizioni per stato con paginazione
    Page<ExamEnrollment> findByStatus(EnrollmentStatus status, Pageable pageable);

    // Trova iscrizioni per studente e stato con paginazione
    Page<ExamEnrollment> findByStudentIdAndStatus(Long studentId, EnrollmentStatus status, Pageable pageable);

    // Trova iscrizioni per esame e studente
    Page<ExamEnrollment> findByExamIdAndStudentId(Long examId, Long studentId, Pageable pageable);

    // Trova iscrizioni per esame, studente e stato
    Page<ExamEnrollment> findByExamIdAndStudentIdAndStatus(Long examId, Long studentId, EnrollmentStatus status, Pageable pageable);
}