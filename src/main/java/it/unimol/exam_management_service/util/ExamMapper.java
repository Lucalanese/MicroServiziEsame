package it.unimol.exam_management_service.util;

import it.unimol.exam_management_service.dto.ExamDTO;
import it.unimol.exam_management_service.entity.Exam;
import org.springframework.stereotype.Component;

@Component
public class ExamMapper {

    public ExamDTO toDTO(Exam exam) {
        if (exam == null) {
            return null;
        }

        ExamDTO dto = new ExamDTO();
        dto.setId(exam.getId());
        dto.setName(exam.getName());
        dto.setDate(exam.getDate());
        dto.setTime(exam.getTime());
        dto.setCourseId(exam.getCourseId());
        dto.setProfessorId(exam.getProfessorId());
        dto.setClassroomId(exam.getClassroomId());
        dto.setMaxStudents(exam.getMaxStudents());
        dto.setEnrollmentDeadline(exam.getEnrollmentDeadline());
        dto.setStatus(exam.getStatus());
        dto.setNotes(exam.getNotes());
        dto.setCurrentEnrollments(exam.getCurrentEnrollmentCount());
        dto.setEnrollmentOpen(exam.isEnrollmentOpen());

        // These fields would typically be populated from external services
        // in a microservice architecture
        // dto.setCourseName("Nome corso");
        // dto.setProfessorName("Nome professore");
        // dto.setClassroomName("Nome aula");

        return dto;
    }

    public Exam toEntity(ExamDTO dto) {
        if (dto == null) {
            return null;
        }

        Exam exam = new Exam();
        exam.setName(dto.getName());
        exam.setDate(dto.getDate());
        exam.setTime(dto.getTime());
        exam.setCourseId(dto.getCourseId());
        exam.setProfessorId(dto.getProfessorId());
        exam.setClassroomId(dto.getClassroomId());
        exam.setMaxStudents(dto.getMaxStudents());
        exam.setEnrollmentDeadline(dto.getEnrollmentDeadline());
        exam.setStatus(dto.getStatus());
        exam.setNotes(dto.getNotes());

        return exam;
    }
}