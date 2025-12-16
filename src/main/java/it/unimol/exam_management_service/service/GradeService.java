package it.unimol.exam_management_service.service;

import it.unimol.exam_management_service.dto.GradeDTO;
import it.unimol.exam_management_service.dto.request.CreateGradeRequest;
import it.unimol.exam_management_service.entity.Exam;
import it.unimol.exam_management_service.entity.ExamEnrollment;
import it.unimol.exam_management_service.entity.ExamGrade;
import it.unimol.exam_management_service.enums.EnrollmentStatus;
import it.unimol.exam_management_service.enums.ExamStatus;
import it.unimol.exam_management_service.enums.GradeStatus;
import it.unimol.exam_management_service.exception.ResourceNotFoundException;
import it.unimol.exam_management_service.repository.EnrollmentRepository;
import it.unimol.exam_management_service.repository.ExamRepository;
import it.unimol.exam_management_service.repository.GradeRepository;
import it.unimol.exam_management_service.util.GradeMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class GradeService {

    private final GradeRepository gradeRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ExamRepository examRepository;
    private final RabbitTemplate rabbitTemplate;
    private final GradeMapper gradeMapper;

    @Autowired
    public GradeService(GradeRepository gradeRepository,
                        EnrollmentRepository enrollmentRepository,
                        ExamRepository examRepository,
                        RabbitTemplate rabbitTemplate,
                        GradeMapper gradeMapper) {
        this.gradeRepository = gradeRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.examRepository = examRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.gradeMapper = gradeMapper;
    }

    @Transactional
    public GradeDTO recordGrade(Long examId, CreateGradeRequest request) {
        // Validate exam exists
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Esame non trovato con ID: " + examId));

        // Validate enrollment exists
        ExamEnrollment enrollment = enrollmentRepository.findById(request.getEnrollmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Iscrizione non trovata con ID: " + request.getEnrollmentId()));

        // Validate enrollment belongs to this exam
        if (!enrollment.getExam().getId().equals(examId)) {
            throw new IllegalArgumentException("L'iscrizione non appartiene a questo esame");
        }

        // Validate enrollment status
        if (enrollment.getStatus() != EnrollmentStatus.PRESENT) {
            throw new IllegalStateException("Lo studente deve essere marcato come presente per ricevere un voto");
        }

        // Validate professor is assigned to the exam
        if (!exam.getProfessorId().equals(request.getProfessorId())) {
            throw new IllegalStateException("Solo il professore assegnato può registrare i voti per questo esame");
        }

        // Check if a grade already exists
        Optional<ExamGrade> existingGrade = gradeRepository.findByEnrollmentId(request.getEnrollmentId());

        ExamGrade grade;
        if (existingGrade.isPresent()) {
            // Update existing grade
            grade = existingGrade.get();
            grade.setGrade(request.getGrade());
            grade.setHonors(request.getHonors());
            grade.setFeedback(request.getFeedback());
            grade.setStatus(GradeStatus.EVALUATED);
        } else {
            // Create new grade
            grade = new ExamGrade(
                    request.getGrade(),
                    request.getHonors(),
                    request.getProfessorId(),
                    enrollment,
                    request.getFeedback()
            );
        }

        ExamGrade savedGrade = gradeRepository.save(grade);

        // Publish event
        rabbitTemplate.convertAndSend("exam-events", "exam.grade.recorded", gradeMapper.toDTO(savedGrade));

        return gradeMapper.toDTO(savedGrade);
    }

    @Transactional(readOnly = true)
    public GradeDTO getGradeById(Long id) {
        ExamGrade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voto non trovato con ID: " + id));

        return gradeMapper.toDTO(grade);
    }

    @Transactional(readOnly = true)
    public List<GradeDTO> getExamGrades(Long examId, Integer minGrade, Integer maxGrade,
                                        Boolean withHonors, Pageable pageable) {
        Page<ExamGrade> grades = gradeRepository.findGradesWithFilters(examId, minGrade, maxGrade, withHonors, pageable);

        return grades.stream()
                .map(gradeMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public GradeDTO updateGrade(Long id, GradeDTO gradeDTO) {
        ExamGrade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voto non trovato con ID: " + id));

        // Check if grade can be modified
        if (!grade.canBeModified()) {
            throw new IllegalStateException("Questo voto non può più essere modificato");
        }

        // Update fields
        grade.setGrade(gradeDTO.getGrade());
        grade.setHonors(gradeDTO.getHonors());
        grade.setFeedback(gradeDTO.getFeedback());

        ExamGrade updatedGrade = gradeRepository.save(grade);

        // Publish event
        rabbitTemplate.convertAndSend("exam-events", "exam.grade.updated", gradeMapper.toDTO(updatedGrade));

        return gradeMapper.toDTO(updatedGrade);
    }

    @Transactional
    public void deleteGrade(Long id) {
        ExamGrade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voto non trovato con ID: " + id));

        // Check if grade can be deleted
        if (!grade.canBeModified()) {
            throw new IllegalStateException("Questo voto non può più essere eliminato");
        }

        gradeRepository.delete(grade);

        // Publish event
        rabbitTemplate.convertAndSend("exam-events", "exam.grade.deleted", id);
    }

    @Transactional(readOnly = true)
    public List<GradeDTO> getMyGrades(Long studentId, Long courseId) {
        List<ExamGrade> grades;

        if (courseId != null) {
            // Filter by course
            grades = gradeRepository.findByStudentIdAndCourseId(studentId, courseId);
        } else {
            // All grades
            grades = gradeRepository.findByStudentId(studentId);
        }

        return grades.stream()
                .map(gradeMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GradeDTO> getStudentGrades(Long studentId, Long courseId) {
        List<ExamGrade> grades;

        if (courseId != null) {
            // Filter by course
            grades = gradeRepository.findByStudentIdAndCourseId(studentId, courseId);
        } else {
            // All grades
            grades = gradeRepository.findByStudentId(studentId);
        }

        return grades.stream()
                .map(gradeMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public GradeDTO publishGrade(Long id) {
        ExamGrade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voto non trovato con ID: " + id));

        grade.publish();
        ExamGrade publishedGrade = gradeRepository.save(grade);

        // Publish event
        rabbitTemplate.convertAndSend("exam-events", "exam.grade.published", gradeMapper.toDTO(publishedGrade));

        return gradeMapper.toDTO(publishedGrade);
    }

    @Transactional
    public void publishAllGradesForExam(Long examId) {
        List<ExamGrade> grades = gradeRepository.findByExamId(examId);

        grades.stream()
                .filter(g -> g.getStatus() == GradeStatus.EVALUATED)
                .forEach(g -> {
                    g.publish();
                    gradeRepository.save(g);

                    // Publish event for each grade
                    rabbitTemplate.convertAndSend("exam-events", "exam.grade.published", gradeMapper.toDTO(g));
                });

        // Update exam status
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new ResourceNotFoundException("Esame non trovato con ID: " + examId));

        exam.setStatus(ExamStatus.COMPLETED);
        examRepository.save(exam);

        // Publish exam completed event
        rabbitTemplate.convertAndSend("exam-events", "exam.completed", examId);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getCourseGradeStatistics(Long courseId) {
        List<ExamGrade> grades = gradeRepository.findByCourseId(courseId);

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalGrades", grades.size());

        List<Integer> validGrades = grades.stream()
                .filter(g -> g.getGrade() != null)
                .map(ExamGrade::getGrade)
                .collect(Collectors.toList());

        if (!validGrades.isEmpty()) {
            statistics.put("averageGrade", validGrades.stream().mapToInt(Integer::intValue).average().orElse(0));
            statistics.put("minGrade", validGrades.stream().mapToInt(Integer::intValue).min().orElse(0));
            statistics.put("maxGrade", validGrades.stream().mapToInt(Integer::intValue).max().orElse(0));
            statistics.put("passedCount", validGrades.stream().filter(g -> g >= 18).count());
            statistics.put("failedCount", validGrades.stream().filter(g -> g < 18).count());
            statistics.put("honorsCount", grades.stream().filter(g -> Boolean.TRUE.equals(g.getHonors())).count());
        }

        return statistics;
    }
}