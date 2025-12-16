package it.unimol.exam_management_service.repository;

import it.unimol.exam_management_service.entity.ExamGrade;
import it.unimol.exam_management_service.enums.GradeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GradeRepository extends JpaRepository<ExamGrade, Long> {

    // Trova voto per un'iscrizione specifica
    Optional<ExamGrade> findByEnrollmentId(Long enrollmentId);
    
    // Voti per esame
    @Query("SELECT g FROM ExamGrade g JOIN g.enrollment e WHERE e.exam.id = :examId")
    List<ExamGrade> findByExamId(@Param("examId") Long examId);
    
    // Voti per studente
    @Query("SELECT g FROM ExamGrade g JOIN g.enrollment e WHERE e.studentId = :studentId")
    List<ExamGrade> findByStudentId(@Param("studentId") Long studentId);
    
    // Voti per corso
    @Query("SELECT g FROM ExamGrade g JOIN g.enrollment e JOIN e.exam ex WHERE ex.courseId = :courseId")
    List<ExamGrade> findByCourseId(@Param("courseId") Long courseId);
    
    // Voti per studente e corso
    @Query("SELECT g FROM ExamGrade g JOIN g.enrollment e JOIN e.exam ex " +
           "WHERE e.studentId = :studentId AND ex.courseId = :courseId")
    List<ExamGrade> findByStudentIdAndCourseId(
            @Param("studentId") Long studentId, 
            @Param("courseId") Long courseId);
    
    // Voti pubblicati per studente
    @Query("SELECT g FROM ExamGrade g JOIN g.enrollment e " +
           "WHERE e.studentId = :studentId AND g.status = 'PUBLISHED'")
    List<ExamGrade> findPublishedGradesByStudentId(@Param("studentId") Long studentId);
    
    // Statistiche voti per esame
    @Query("SELECT MIN(g.grade) as minGrade, MAX(g.grade) as maxGrade, AVG(g.grade) as avgGrade, " +
           "COUNT(g) as totalGrades, SUM(CASE WHEN g.grade >= 18 THEN 1 ELSE 0 END) as passedCount " +
           "FROM ExamGrade g JOIN g.enrollment e WHERE e.exam.id = :examId AND g.grade IS NOT NULL")
    Object[] getExamStatistics(@Param("examId") Long examId);
    
    // Voti filtrati per valore
    @Query("SELECT g FROM ExamGrade g JOIN g.enrollment e WHERE e.exam.id = :examId " +
           "AND (g.grade >= :minGrade OR :minGrade IS NULL) " +
           "AND (g.grade <= :maxGrade OR :maxGrade IS NULL) " +
           "AND (g.honors = :withHonors OR :withHonors IS NULL)")
    Page<ExamGrade> findGradesWithFilters(
            @Param("examId") Long examId,
            @Param("minGrade") Integer minGrade,
            @Param("maxGrade") Integer maxGrade,
            @Param("withHonors") Boolean withHonors,
            Pageable pageable);
            
    // Conta voti per status
    long countByStatus(GradeStatus status);
}