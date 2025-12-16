package it.unimol.exam_management_service.service;

import it.unimol.exam_management_service.dto.ExamDTO;
import it.unimol.exam_management_service.dto.request.CreateExamRequest;
import it.unimol.exam_management_service.entity.Exam;
import it.unimol.exam_management_service.enums.ExamStatus;
import it.unimol.exam_management_service.exception.ResourceNotFoundException;
import it.unimol.exam_management_service.repository.EnrollmentRepository;
import it.unimol.exam_management_service.repository.ExamRepository;
import it.unimol.exam_management_service.util.ExamMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExamService {

    private final ExamRepository examRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ExamMapper examMapper;

    @Autowired
    public ExamService(ExamRepository examRepository,
                       EnrollmentRepository enrollmentRepository,
                       RabbitTemplate rabbitTemplate,
                       ExamMapper examMapper) {
        this.examRepository = examRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.examMapper = examMapper;
    }

    @Transactional
    public ExamDTO createExam(CreateExamRequest request) {
        // Validate enrollment deadline is before exam date
        if (request.getEnrollmentDeadline().isAfter(request.getDate())) {
            throw new IllegalArgumentException("La scadenza per le iscrizioni deve essere prima della data dell'esame");
        }

        Exam exam = new Exam(
                request.getName(),
                request.getDate(),
                request.getTime(),
                request.getCourseId(),
                request.getProfessorId(),
                request.getClassroomId(),
                request.getMaxStudents(),
                request.getEnrollmentDeadline()
        );
        exam.setNotes(request.getNotes());
        exam.setStatus(ExamStatus.SCHEDULED);

        Exam savedExam = examRepository.save(exam);

        // Publish event
        rabbitTemplate.convertAndSend("exam-events", "exam.created", examMapper.toDTO(savedExam));

        return examMapper.toDTO(savedExam);
    }

    @Transactional(readOnly = true)
    public ExamDTO getExamById(Long id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Esame non trovato con ID: " + id));

        ExamDTO dto = examMapper.toDTO(exam);
        dto.setCurrentEnrollments((int) enrollmentRepository.countByExamId(id));
        return dto;
    }

    @Transactional(readOnly = true)
    public List<ExamDTO> getAllExams(LocalDate startDate, LocalDate endDate,
                                     Long courseId, Long professorId,
                                     Pageable pageable) {
        Page<Exam> exams;

        if (startDate != null && endDate != null) {
            // Filter by date range
            if (courseId != null) {
                // Filter by course and date range
                exams = examRepository.findByCourseIdAndStatusInAndDateBetween(
                        courseId,
                        List.of(ExamStatus.values()),
                        startDate,
                        endDate,
                        pageable);
            } else if (professorId != null) {
                // Filter by professor
                exams = examRepository.findAll(pageable);
                // Additional filtering will be applied in the stream below
            } else {
                // Only date filter
                exams = examRepository.findAll(pageable);
                // Additional filtering will be applied in the stream below
            }
        } else {
            // No date filter
            exams = examRepository.findAll(pageable);
        }

        return exams.stream()
                .filter(e -> professorId == null || e.getProfessorId().equals(professorId))
                .filter(e -> startDate == null || !e.getDate().isBefore(startDate))
                .filter(e -> endDate == null || !e.getDate().isAfter(endDate))
                .map(exam -> {
                    ExamDTO dto = examMapper.toDTO(exam);
                    dto.setCurrentEnrollments((int) enrollmentRepository.countByExamId(exam.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public ExamDTO updateExam(Long id, ExamDTO examDTO) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Esame non trovato con ID: " + id));

        // Validate enrollment deadline is before exam date
        if (examDTO.getEnrollmentDeadline().isAfter(examDTO.getDate())) {
            throw new IllegalArgumentException("La scadenza per le iscrizioni deve essere prima della data dell'esame");
        }

        // Update fields
        exam.setName(examDTO.getName());
        exam.setDate(examDTO.getDate());
        exam.setTime(examDTO.getTime());
        exam.setCourseId(examDTO.getCourseId());
        exam.setProfessorId(examDTO.getProfessorId());
        exam.setClassroomId(examDTO.getClassroomId());
        exam.setMaxStudents(examDTO.getMaxStudents());
        exam.setEnrollmentDeadline(examDTO.getEnrollmentDeadline());
        exam.setNotes(examDTO.getNotes());

        if (examDTO.getStatus() != null) {
            exam.setStatus(examDTO.getStatus());
        }

        Exam updatedExam = examRepository.save(exam);

        // Publish event
        rabbitTemplate.convertAndSend("exam-events", "exam.updated", examMapper.toDTO(updatedExam));

        return examMapper.toDTO(updatedExam);
    }

    @Transactional
    public void deleteExam(Long id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Esame non trovato con ID: " + id));

        // Check if exam has enrollments
        long enrollmentCount = enrollmentRepository.countByExamId(id);
        if (enrollmentCount > 0) {
            throw new IllegalStateException("Impossibile eliminare l'esame: ci sono " + enrollmentCount + " iscrizioni associate");
        }

        examRepository.delete(exam);

        // Publish event
        rabbitTemplate.convertAndSend("exam-events", "exam.deleted", id);
    }

    @Transactional(readOnly = true)
    public List<ExamDTO> getExamsByCourse(Long courseId) {
        return examRepository.findByCourseIdOrderByDateDesc(courseId).stream()
                .map(exam -> {
                    ExamDTO dto = examMapper.toDTO(exam);
                    dto.setCurrentEnrollments((int) enrollmentRepository.countByExamId(exam.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExamDTO> getExamsByProfessor(Long professorId) {
        return examRepository.findByProfessorIdOrderByDateDesc(professorId).stream()
                .map(exam -> {
                    ExamDTO dto = examMapper.toDTO(exam);
                    dto.setCurrentEnrollments((int) enrollmentRepository.countByExamId(exam.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExamDTO> getAvailableExams(Long studentId) {
        LocalDate today = LocalDate.now();
        List<Exam> exams;

        if (studentId != null) {
            // Get available exams for specific student (exams they're not already enrolled in)
            exams = examRepository.findAvailableExamsForStudent(today, studentId);
        } else {
            // Get all available exams
            exams = examRepository.findAvailableExams(today);
        }

        return exams.stream()
                .map(exam -> {
                    ExamDTO dto = examMapper.toDTO(exam);
                    dto.setCurrentEnrollments((int) enrollmentRepository.countByExamId(exam.getId()));
                    dto.setEnrollmentOpen(true);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateExamStatus(Long id, ExamStatus status) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Esame non trovato con ID: " + id));

        exam.setStatus(status);
        examRepository.save(exam);

        // Publish event
        rabbitTemplate.convertAndSend("exam-events", "exam.status.updated", examMapper.toDTO(exam));
    }

    @Transactional(readOnly = true)
    public List<ExamDTO> getExamCalendar(LocalDate startDate, LocalDate endDate,
                                         Long courseId, Long professorId) {
        LocalDate defaultStartDate = LocalDate.now();
        LocalDate defaultEndDate = defaultStartDate.plusMonths(3);

        startDate = startDate != null ? startDate : defaultStartDate;
        endDate = endDate != null ? endDate : defaultEndDate;

        List<Exam> exams = examRepository.findByDateBetweenOrderByDateAsc(startDate, endDate);

        return exams.stream()
                .filter(e -> courseId == null || e.getCourseId().equals(courseId))
                .filter(e -> professorId == null || e.getProfessorId().equals(professorId))
                .map(exam -> {
                    ExamDTO dto = examMapper.toDTO(exam);
                    dto.setCurrentEnrollments((int) enrollmentRepository.countByExamId(exam.getId()));
                    dto.setEnrollmentOpen(exam.isEnrollmentOpen());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}