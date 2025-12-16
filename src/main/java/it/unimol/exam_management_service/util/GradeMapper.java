package it.unimol.exam_management_service.util;

import it.unimol.exam_management_service.dto.GradeDTO;
import it.unimol.exam_management_service.entity.ExamGrade;
import org.springframework.stereotype.Component;

@Component
public class GradeMapper {

    public GradeDTO toDTO(ExamGrade grade) {
        if (grade == null) {
            return null;
        }

        GradeDTO dto = new GradeDTO();
        dto.setId(grade.getId());
        dto.setEnrollmentId(grade.getEnrollment().getId());
        dto.setExamId(grade.getEnrollment().getExam().getId());
        dto.setExamName(grade.getEnrollment().getExam().getName());
        dto.setStudentId(grade.getEnrollment().getStudentId());
        dto.setGrade(grade.getGrade());
        dto.setHonors(grade.getHonors());
        dto.setStatus(grade.getStatus());
        dto.setEvaluationDate(grade.getEvaluationDate());
        dto.setProfessorId(grade.getProfessorId());
        dto.setFeedback(grade.getFeedback());
        dto.setPublishedDate(grade.getPublishedDate());
        dto.setFormattedGrade(grade.getFormattedGrade());

        // These fields would typically be populated from external services
        // in a microservice architecture
        // dto.setStudentName("Nome studente");
        // dto.setProfessorName("Nome professore");

        return dto;
    }
}