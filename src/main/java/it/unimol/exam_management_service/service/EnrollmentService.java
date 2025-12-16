package it.unimol.exam_management_service.service;

import it.unimol.exam_management_service.dto.EnrollmentDTO;
import it.unimol.exam_management_service.dto.request.EnrollmentRequest;
import it.unimol.exam_management_service.entity.Exam;
import it.unimol.exam_management_service.entity.ExamEnrollment;
import it.unimol.exam_management_service.entity.ExamGrade;
import it.unimol.exam_management_service.enums.EnrollmentStatus;
import it.unimol.exam_management_service.exception.ResourceNotFoundException;
import it.unimol.exam_management_service.util.EnrollmentMapper;
import it.unimol.exam_management_service.repository.EnrollmentRepository;
import it.unimol.exam_management_service.repository.ExamRepository;
import it.unimol.exam_management_service.security.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EnrollmentService {
    private final Logger logger = LoggerFactory.getLogger(EnrollmentService.class);

    private final EnrollmentRepository enrollmentRepository;
    private final ExamRepository examRepository;
    private final EnrollmentMapper enrollmentMapper;
    private final SecurityUtils securityUtils;

    @Autowired
    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             ExamRepository examRepository,
                             EnrollmentMapper enrollmentMapper,
                             SecurityUtils securityUtils) {
        this.enrollmentRepository = enrollmentRepository;
        this.examRepository = examRepository;
        this.enrollmentMapper = enrollmentMapper;
        this.securityUtils = securityUtils;
    }

    @Transactional
    public EnrollmentDTO createEnrollment(Long studentId, Long examId) {
        logger.debug("Creazione iscrizione per studente ID: {} all'esame ID: {}", studentId, examId);

        // Verifica che lo studente sia quello corretto o un admin
        if (!securityUtils.isStudentOrAdmin(studentId)) {
            logger.warn("Tentativo non autorizzato di iscrizione per studente ID: {}", studentId);
            throw new AccessDeniedException("Non sei autorizzato ad iscrivere questo studente");
        }

        // Verifica che l'esame esista
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> {
                    logger.error("Esame non trovato con ID: {}", examId);
                    return new ResourceNotFoundException("Esame non trovato con ID: " + examId);
                });

        // Verifica che l'esame sia ancora aperto per le iscrizioni
        if (LocalDateTime.now().isAfter(exam.getDate().minusDays(1).atStartOfDay())) {
            logger.error("Iscrizione non possibile, esame ID: {} è troppo vicino", examId);
            throw new IllegalStateException("L'iscrizione non è più possibile. Il termine per iscriversi è scaduto.");
        }

        // Verifica che lo studente non sia già iscritto
        if (enrollmentRepository.existsByStudentIdAndExamId(studentId, examId)) {
            logger.error("Studente ID: {} già iscritto all'esame ID: {}", studentId, examId);
            throw new IllegalStateException("Sei già iscritto a questo esame");
        }

        // Crea la nuova iscrizione
        ExamEnrollment enrollment = new ExamEnrollment();
        enrollment.setStudentId(studentId);
        enrollment.setExam(exam);
        enrollment.setEnrollmentDate(LocalDateTime.now());
        enrollment.setStatus(EnrollmentStatus.ENROLLED);

        // Salva e restituisci
        ExamEnrollment savedEnrollment = enrollmentRepository.save(enrollment);
        logger.info("Iscrizione creata con successo per studente ID: {} all'esame ID: {}", studentId, examId);
        return enrollmentMapper.toDTO(savedEnrollment);
    }

    /**
     * Iscrive uno studente a un esame utilizzando la request
     */
    @Transactional
    public EnrollmentDTO enrollToExam(Long examId, EnrollmentRequest request) {
        logger.debug("Creazione iscrizione per studente ID: {} all'esame ID: {}", request.getStudentId(), examId);

        // Verifica che lo studente sia quello corretto o un admin
        if (!securityUtils.isStudentOrAdmin(request.getStudentId())) {
            logger.warn("Tentativo non autorizzato di iscrizione per studente ID: {}", request.getStudentId());
            throw new AccessDeniedException("Non sei autorizzato ad iscrivere questo studente");
        }

        // Verifica che l'esame esista
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> {
                    logger.error("Esame non trovato con ID: {}", examId);
                    return new ResourceNotFoundException("Esame non trovato con ID: " + examId);
                });

        // Verifica che l'esame sia ancora aperto per le iscrizioni
        if (LocalDateTime.now().isAfter(exam.getEnrollmentDeadline().atStartOfDay())) {
            logger.error("Iscrizione non possibile, termine iscrizioni per esame ID: {} è scaduto", examId);
            throw new IllegalStateException("L'iscrizione non è più possibile. Il termine per iscriversi è scaduto.");
        }

        // Verifica che lo studente non sia già iscritto
        if (enrollmentRepository.existsByStudentIdAndExamId(request.getStudentId(), examId)) {
            logger.error("Studente ID: {} già iscritto all'esame ID: {}", request.getStudentId(), examId);
            throw new IllegalStateException("Sei già iscritto a questo esame");
        }

        // Crea la nuova iscrizione
        ExamEnrollment enrollment = new ExamEnrollment();
        enrollment.setStudentId(request.getStudentId());
        enrollment.setExam(exam);
        enrollment.setEnrollmentDate(LocalDateTime.now());
        enrollment.setStatus(EnrollmentStatus.ENROLLED);
        enrollment.setNotes(request.getNotes());

        // Salva e restituisci
        ExamEnrollment savedEnrollment = enrollmentRepository.save(enrollment);
        logger.info("Iscrizione creata con successo per studente ID: {} all'esame ID: {}", request.getStudentId(), examId);
        return enrollmentMapper.toDTO(savedEnrollment);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentDTO> getAllEnrollments(Long examId, Long studentId,
                                                 EnrollmentStatus status, Pageable pageable) {
        logger.debug("Recupero iscrizioni con filtri - esame: {}, studente: {}, stato: {}",
                examId, studentId, status);

        // Verifica che l'utente sia un admin
        if (!securityUtils.isAdmin()) {
            logger.warn("Tentativo non autorizzato di accesso a tutte le iscrizioni");
            throw new AccessDeniedException("Solo gli amministratori possono visualizzare tutte le iscrizioni");
        }

        // Costruisci la query in base ai filtri forniti
        Page<ExamEnrollment> enrollmentsPage;

        if (examId != null && studentId != null && status != null) {
            enrollmentsPage = enrollmentRepository.findByExamIdAndStudentIdAndStatus(examId, studentId, status, pageable);
        } else if (examId != null && studentId != null) {
            enrollmentsPage = enrollmentRepository.findByExamIdAndStudentId(examId, studentId, pageable);
        } else if (examId != null && status != null) {
            enrollmentsPage = enrollmentRepository.findByExamIdAndStatus(examId, status, pageable);
        } else if (studentId != null && status != null) {
            enrollmentsPage = enrollmentRepository.findByStudentIdAndStatus(studentId, status, pageable);
        } else if (examId != null) {
            enrollmentsPage = enrollmentRepository.findByExamId(examId, pageable);
        } else if (studentId != null) {
            enrollmentsPage = enrollmentRepository.findByStudentId(studentId, pageable);
        } else if (status != null) {
            enrollmentsPage = enrollmentRepository.findByStatus(status, pageable);
        } else {
            enrollmentsPage = enrollmentRepository.findAll(pageable);
        }

        return enrollmentsPage.getContent().stream()
                .map(enrollmentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EnrollmentDTO getEnrollmentById(Long enrollmentId) {
        logger.debug("Recupero iscrizione con ID: {}", enrollmentId);

        ExamEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> {
                    logger.error("Iscrizione non trovata con ID: {}", enrollmentId);
                    return new ResourceNotFoundException("Iscrizione non trovata con ID: " + enrollmentId);
                });

        // Verifica autorizzazioni: solo lo studente iscritto, docenti o admin possono vedere
        if (!securityUtils.isStudentOrAdmin(enrollment.getStudentId()) && !securityUtils.isDocente()) {
            logger.warn("Tentativo non autorizzato di accesso all'iscrizione ID: {}", enrollmentId);
            throw new AccessDeniedException("Non sei autorizzato a visualizzare questa iscrizione");
        }

        return enrollmentMapper.toDTO(enrollment);
    }

    /**
     * Ottiene le iscrizioni di uno studente specifico senza paginazione
     */
    @Transactional(readOnly = true)
    public List<EnrollmentDTO> getMyEnrollments(Long studentId, EnrollmentStatus status) {
        logger.debug("Recupero iscrizioni per studente ID: {}, stato: {}", studentId, status);

        // Verifica che l'utente sia lo studente stesso o un admin
        if (!securityUtils.isStudentOrAdmin(studentId)) {
            logger.warn("Tentativo non autorizzato di accesso alle iscrizioni dello studente ID: {}", studentId);
            throw new AccessDeniedException("Non sei autorizzato a visualizzare le iscrizioni di questo studente");
        }

        List<ExamEnrollment> enrollments;

        if (status != null) {
            // Filtra per stato
            enrollments = enrollmentRepository.findByStudentIdAndStatusOrderByEnrollmentDateDesc(studentId, status);
        } else {
            // Prendi tutte le iscrizioni dello studente
            enrollments = enrollmentRepository.findByStudentIdOrderByEnrollmentDateDesc(studentId);
        }

        return enrollments.stream()
                .map(enrollmentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EnrollmentDTO> getStudentEnrollments(Long studentId, EnrollmentStatus status, Pageable pageable) {
        logger.debug("Recupero iscrizioni per studente ID: {}, stato: {}", studentId, status);

        // Verifica che l'utente sia lo studente stesso o un admin
        if (!securityUtils.isStudentOrAdmin(studentId)) {
            logger.warn("Tentativo non autorizzato di accesso alle iscrizioni dello studente ID: {}", studentId);
            throw new AccessDeniedException("Non sei autorizzato a visualizzare le iscrizioni di questo studente");
        }

        Page<ExamEnrollment> enrollments;

        if (status != null) {
            // Filtra per stato
            enrollments = enrollmentRepository.findByStudentIdAndStatus(studentId, status, pageable);
        } else {
            // Prendi tutte le iscrizioni dello studente
            enrollments = enrollmentRepository.findByStudentId(studentId, pageable);
        }

        return enrollments.getContent().stream()
                .map(enrollmentMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Ottiene le iscrizioni di uno studente specifico senza paginazione
     */
    @Transactional(readOnly = true)
    public List<EnrollmentDTO> getStudentEnrollments(Long studentId, EnrollmentStatus status) {
        logger.debug("Recupero iscrizioni per studente ID: {}, stato: {}", studentId, status);

        // Verifica che l'utente sia lo studente stesso, un docente o un admin
        if (!securityUtils.isStudentOrAdmin(studentId) && !securityUtils.isDocente()) {
            logger.warn("Tentativo non autorizzato di accesso alle iscrizioni dello studente ID: {}", studentId);
            throw new AccessDeniedException("Non sei autorizzato a visualizzare le iscrizioni di questo studente");
        }

        List<ExamEnrollment> enrollments;

        if (status != null) {
            // Filtra per stato
            enrollments = enrollmentRepository.findByStudentIdAndStatusOrderByEnrollmentDateDesc(studentId, status);
        } else {
            // Prendi tutte le iscrizioni dello studente
            enrollments = enrollmentRepository.findByStudentIdOrderByEnrollmentDateDesc(studentId);
        }

        return enrollments.stream()
                .map(enrollmentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EnrollmentDTO> getExamEnrollments(Long examId, EnrollmentStatus status, Pageable pageable) {
        logger.debug("Recupero iscrizioni per esame ID: {}, stato: {}", examId, status);

        // Verifica che l'esame esista
        if (!examRepository.existsById(examId)) {
            logger.error("Esame non trovato con ID: {}", examId);
            throw new ResourceNotFoundException("Esame non trovato con ID: " + examId);
        }

        // Verifica che l'utente sia un docente o admin
        if (!securityUtils.isDocente() && !securityUtils.isAdmin()) {
            logger.warn("Tentativo non autorizzato di accesso alle iscrizioni dell'esame ID: {}", examId);
            throw new AccessDeniedException("Solo docenti e amministratori possono visualizzare le iscrizioni agli esami");
        }

        Page<ExamEnrollment> enrollments;

        if (status != null) {
            // Filter by status
            enrollments = enrollmentRepository.findByExamIdAndStatus(examId, status, pageable);
        } else {
            // Get all enrollments for the exam
            enrollments = enrollmentRepository.findByExamId(examId, pageable);
        }

        return enrollments.getContent().stream()
                .map(enrollmentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public EnrollmentDTO withdrawEnrollment(Long studentId, Long enrollmentId) {
        logger.debug("Ritiro iscrizione ID: {} per studente ID: {}", enrollmentId, studentId);

        ExamEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> {
                    logger.error("Iscrizione non trovata con ID: {}", enrollmentId);
                    return new ResourceNotFoundException("Iscrizione non trovata con ID: " + enrollmentId);
                });

        // Verifica che sia lo studente corretto o un admin
        if (!securityUtils.isStudentOrAdmin(studentId)) {
            logger.warn("Tentativo non autorizzato di cancellare iscrizione ID: {} per studente ID: {}",
                    enrollmentId, studentId);
            throw new AccessDeniedException("Non sei autorizzato a cancellare questa iscrizione");
        }

        // Verifica che l'iscrizione appartenga allo studente
        if (!enrollment.getStudentId().equals(studentId)) {
            logger.error("Iscrizione ID: {} non appartiene allo studente ID: {}", enrollmentId, studentId);
            throw new IllegalArgumentException("L'iscrizione non appartiene a questo studente");
        }

        // Verifica che l'iscrizione possa essere ritirata (esame non ancora svolto)
        if (LocalDateTime.now().isAfter(enrollment.getExam().getDate().atStartOfDay())) {
            logger.error("Impossibile ritirare iscrizione ID: {} - l'esame è già passato", enrollmentId);
            throw new IllegalStateException("Non è possibile ritirare l'iscrizione poiché l'esame è già stato svolto");
        }

        // Verifica che l'iscrizione sia nello stato ENROLLED
        if (enrollment.getStatus() != EnrollmentStatus.ENROLLED) {
            logger.error("Impossibile ritirare iscrizione ID: {} - stato attuale: {}",
                    enrollmentId, enrollment.getStatus());
            throw new IllegalStateException("Non è possibile ritirare un'iscrizione che non è nello stato ENROLLED");
        }

        // Aggiorna lo stato dell'iscrizione
        enrollment.setStatus(EnrollmentStatus.WITHDREW);
        ExamEnrollment updatedEnrollment = enrollmentRepository.save(enrollment);
        logger.info("Iscrizione ID: {} ritirata con successo", enrollmentId);

        return enrollmentMapper.toDTO(updatedEnrollment);
    }

    /**
     * Cancella l'iscrizione di uno studente
     */
    @Transactional
    public void cancelEnrollment(Long enrollmentId, Long studentId) {
        logger.debug("Cancellazione iscrizione ID: {} per studente ID: {}", enrollmentId, studentId);

        ExamEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> {
                    logger.error("Iscrizione non trovata con ID: {}", enrollmentId);
                    return new ResourceNotFoundException("Iscrizione non trovata con ID: " + enrollmentId);
                });

        // Verifica che sia lo studente corretto o un admin
        if (!securityUtils.isStudentOrAdmin(studentId)) {
            logger.warn("Tentativo non autorizzato di cancellare iscrizione ID: {} per studente ID: {}",
                    enrollmentId, studentId);
            throw new AccessDeniedException("Non sei autorizzato a cancellare questa iscrizione");
        }

        // Verifica che l'iscrizione appartenga allo studente
        if (!enrollment.getStudentId().equals(studentId)) {
            logger.error("Iscrizione ID: {} non appartiene allo studente ID: {}", enrollmentId, studentId);
            throw new IllegalArgumentException("L'iscrizione non appartiene a questo studente");
        }

        // Verifica che l'iscrizione possa essere cancellata (esame non ancora svolto)
        if (LocalDateTime.now().isAfter(enrollment.getExam().getDate().atStartOfDay())) {
            logger.error("Impossibile cancellare iscrizione ID: {} - l'esame è già passato", enrollmentId);
            throw new IllegalStateException("Non è possibile cancellare l'iscrizione poiché l'esame è già stato svolto");
        }

        // Verifica che l'iscrizione sia nello stato ENROLLED
        if (enrollment.getStatus() != EnrollmentStatus.ENROLLED) {
            logger.error("Impossibile cancellare iscrizione ID: {} - stato attuale: {}",
                    enrollmentId, enrollment.getStatus());
            throw new IllegalStateException("Non è possibile cancellare un'iscrizione che non è nello stato ENROLLED");
        }

        // Cancella l'iscrizione
        enrollmentRepository.delete(enrollment);
        logger.info("Iscrizione ID: {} cancellata con successo", enrollmentId);
    }

    /**
     * Aggiorna lo stato di un'iscrizione
     */
    @Transactional
    public EnrollmentDTO updateEnrollmentStatus(Long enrollmentId, EnrollmentStatus status) {
        logger.debug("Aggiornamento stato iscrizione ID: {} a {}", enrollmentId, status);

        // Verifica che l'utente sia un docente o admin
        if (!securityUtils.isDocente() && !securityUtils.isAdmin()) {
            logger.warn("Tentativo non autorizzato di aggiornare lo stato dell'iscrizione ID: {}", enrollmentId);
            throw new AccessDeniedException("Solo docenti e amministratori possono aggiornare lo stato delle iscrizioni");
        }

        ExamEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> {
                    logger.error("Iscrizione non trovata con ID: {}", enrollmentId);
                    return new ResourceNotFoundException("Iscrizione non trovata con ID: " + enrollmentId);
                });

        // Verifica logica per il cambio di stato
        if (status == EnrollmentStatus.WITHDREW && enrollment.getStatus() != EnrollmentStatus.ENROLLED) {
            throw new IllegalStateException("Solo un'iscrizione nello stato ENROLLED può essere ritirata");
        } else if (status == EnrollmentStatus.GRADED && enrollment.getStatus() != EnrollmentStatus.ENROLLED
                && enrollment.getStatus() != EnrollmentStatus.PRESENT) {
            throw new IllegalStateException("Solo un'iscrizione nello stato ENROLLED o PRESENT può essere valutata");
        }

        enrollment.setStatus(status);
        ExamEnrollment updatedEnrollment = enrollmentRepository.save(enrollment);
        logger.info("Stato iscrizione ID: {} aggiornato a {} con successo", enrollmentId, status);

        return enrollmentMapper.toDTO(updatedEnrollment);
    }

    @Transactional
    public EnrollmentDTO updateEnrollmentGrade(Long enrollmentId, ExamGrade grade, String notes) {
        logger.debug("Aggiornamento voto per iscrizione ID: {}", enrollmentId);

        // Verifica che l'utente sia un docente o admin
        if (!securityUtils.isDocente() && !securityUtils.isAdmin()) {
            logger.warn("Tentativo non autorizzato di aggiornare il voto per l'iscrizione ID: {}", enrollmentId);
            throw new AccessDeniedException("Solo docenti e amministratori possono aggiornare i voti");
        }

        ExamEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> {
                    logger.error("Iscrizione non trovata con ID: {}", enrollmentId);
                    return new ResourceNotFoundException("Iscrizione non trovata con ID: " + enrollmentId);
                });

        // Verifica che l'esame sia già stato svolto
        if (LocalDateTime.now().isBefore(enrollment.getExam().getDate().atStartOfDay())) {
            logger.error("Impossibile registrare voto per iscrizione ID: {} - l'esame non è ancora stato svolto", enrollmentId);
            throw new IllegalStateException("Non è possibile registrare un voto per un esame non ancora svolto");
        }

        // Verifica che l'iscrizione sia nello stato ENROLLED o GRADED
        if (enrollment.getStatus() != EnrollmentStatus.ENROLLED && enrollment.getStatus() != EnrollmentStatus.GRADED) {
            logger.error("Impossibile registrare voto per iscrizione ID: {} - stato attuale: {}",
                    enrollmentId, enrollment.getStatus());
            throw new IllegalStateException("Non è possibile registrare un voto per un'iscrizione che non è nello stato ENROLLED o GRADED");
        }

        // Aggiorna il voto e lo stato
        enrollment.setGrade(grade);
        enrollment.setStatus(EnrollmentStatus.GRADED);

        // Aggiorna le note se fornite
        if (notes != null && !notes.trim().isEmpty()) {
            enrollment.setNotes(notes);
        }

        ExamEnrollment updatedEnrollment = enrollmentRepository.save(enrollment);
        logger.info("Voto registrato con successo per iscrizione ID: {}", enrollmentId);

        return enrollmentMapper.toDTO(updatedEnrollment);
    }

    @Transactional(readOnly = true)
    public long countEnrollmentsForExam(Long examId) {
        logger.debug("Conteggio iscrizioni per esame ID: {}", examId);

        // Verifica che l'esame esista
        if (!examRepository.existsById(examId)) {
            logger.error("Esame non trovato con ID: {}", examId);
            throw new ResourceNotFoundException("Esame non trovato con ID: " + examId);
        }

        return enrollmentRepository.countByExamId(examId);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentDTO> getActiveEnrollmentsByStudent(Long studentId) {
        logger.debug("Recupero iscrizioni attive per studente ID: {}", studentId);

        // Verifica che l'utente sia lo studente stesso o un admin
        if (!securityUtils.isStudentOrAdmin(studentId)) {
            logger.warn("Tentativo non autorizzato di accesso alle iscrizioni attive dello studente ID: {}", studentId);
            throw new AccessDeniedException("Non sei autorizzato a visualizzare le iscrizioni di questo studente");
        }

        List<ExamEnrollment> activeEnrollments = enrollmentRepository.findActiveEnrollmentsByStudent(studentId);

        return activeEnrollments.stream()
                .map(enrollmentMapper::toDTO)
                .collect(Collectors.toList());
    }
}