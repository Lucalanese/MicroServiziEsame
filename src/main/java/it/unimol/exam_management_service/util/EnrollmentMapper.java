package it.unimol.exam_management_service.util;

import it.unimol.exam_management_service.dto.EnrollmentDTO;
import it.unimol.exam_management_service.entity.ExamEnrollment;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentMapper {

    public EnrollmentDTO toDTO(ExamEnrollment enrollment) {
        if (enrollment == null) {
            return null;
        }

        EnrollmentDTO dto = new EnrollmentDTO();
        dto.setId(enrollment.getId());
        dto.setExamId(enrollment.getExam().getId());
        dto.setExamName(enrollment.getExam().getName());
        dto.setExamDate(enrollment.getExam().getDate());
        dto.setStudentId(enrollment.getStudentId());
        dto.setStatus(enrollment.getStatus());
        dto.setEnrollmentDate(enrollment.getEnrollmentDate());
        dto.setNotes(enrollment.getNotes());
        dto.setHasGrade(enrollment.hasGrade());

        if (enrollment.hasGrade()) {
            dto.setGrade(enrollment.getGrade().getGrade());
        }

        // This field would typically be populated from an external service
        // in a microservice architecture
        // dto.setStudentName("Nome studente");

        return dto;
    }
}