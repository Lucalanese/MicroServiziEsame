package it.unimol.exam_management_service.repository;

import it.unimol.exam_management_service.entity.Exam;
import it.unimol.exam_management_service.enums.ExamStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ExamRepository extends JpaRepository<Exam, Long> {

    // Esami futuri per un corso specifico
    List<Exam> findByCourseIdAndDateAfterOrderByDateAsc(Long courseId, LocalDate date);

    List<Exam> findByCourseIdOrderByDateDesc(Long courseId);


    // Esami per professore
    List<Exam> findByProfessorIdOrderByDateDesc(Long professorId);
    
    // Esami tra due date
    List<Exam> findByDateBetweenOrderByDateAsc(LocalDate startDate, LocalDate endDate);
    
    // Ricerca paginata con filtri
    Page<Exam> findByCourseIdAndStatusInAndDateBetween(
            Long courseId, 
            List<ExamStatus> statuses, 
            LocalDate startDate, 
            LocalDate endDate, 
            Pageable pageable);
            
    // Esami disponibili per le iscrizioni
    @Query("SELECT e FROM Exam e WHERE e.status = 'SCHEDULED' AND e.enrollmentDeadline >= :today AND " +
           "(SELECT COUNT(en) FROM ExamEnrollment en WHERE en.exam = e) < e.maxStudents")
    List<Exam> findAvailableExams(@Param("today") LocalDate today);
    
    // Esami per corso con stato specifico
    List<Exam> findByCourseIdAndStatus(Long courseId, ExamStatus status);
    
    // Trova esami futuri a cui uno studente può iscriversi
    @Query("SELECT e FROM Exam e WHERE e.status = 'SCHEDULED' AND e.enrollmentDeadline >= :today " +
           "AND e.date > :today " +
           "AND (SELECT COUNT(en) FROM ExamEnrollment en WHERE en.exam = e) < e.maxStudents " +
           "AND NOT EXISTS (SELECT 1 FROM ExamEnrollment en WHERE en.exam = e AND en.studentId = :studentId)")
    List<Exam> findAvailableExamsForStudent(@Param("today") LocalDate today, @Param("studentId") Long studentId);
}