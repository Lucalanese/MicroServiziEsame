# Microservizio Gestione Esami

## Panoramica

Questo Microservizio è responsabile della gestione completa degli esami all'interno della piattaforma universitaria, fornendo funzionalità per la pianificazione, iscrizione, conduzione e valutazione degli esami:

- **(Amministrativi)** Pianificazione e gestione del calendario esami
- **(Studenti)** Iscrizione agli esami e visualizzazione risultati  
- **(Docenti)** Registrazione voti e gestione delle valutazioni
- **(Tutti)** Visualizzazione del calendario esami e consultazione informazioni

## Tech Stack

- **Framework:** SpringBoot
- **Message Broker:** RabbitMQ
- **Database:** MySQL
- **Containerization:** Docker (non presente su questa repo)
- **Orchestration:** Kubernetes (non presente su questa repo)
- **API Documentation:** Swagger/OpenAPI 3.0

## Modello Dati

### DTO

DataTransferObject presenti nel microservizio.

#### DTO per l'Esame

```java
public class ExamDTO {
    private Long id;
    private Long courseId;
    private String courseName;
    private Long teacherId;
    private String teacherName;
    private LocalDate examDate;
    private LocalTime examTime;
    private String classroom;
    private ExamStatus status;
    private Integer maxEnrollments;
    private Integer currentEnrollments;
    private LocalDateTime creationDate;
}
```

#### DTO per l'Iscrizione Esame

```java
public class EnrollmentDTO {
    private Long id;
    private Long examId;
    private Long studentId;
    private String studentName;
    private EnrollmentStatus status;
    private LocalDateTime enrollmentDate;
    private String notes;
    private String adminNotes;
}
```

#### DTO per il Voto Esame

```java
public class GradeDTO {
    private Long id;
    private Long enrollmentId;
    private Long studentId;
    private String studentName;
    private Long examId;
    private Integer grade;
    private Boolean withHonors;
    private LocalDateTime recordingDate;
    private String notes;
    private String feedback;
}
```

#### DTO per il Calendario Esami

```java
public class ExamCalendarDTO {
    private Long examId;
    private String courseCode;
    private String courseName;
    private String teacherName;
    private LocalDate examDate;
    private LocalTime examTime;
    private String classroom;
    private Integer availableSlots;
    private ExamStatus status;
}
```

### Entità principali JPA

Tabelle in MySQL per strutture dati del microservizio

#### Exam (Esame)

- `id` - ID esame
- `course_id` - ID corso (riferimento esterno)
- `teacher_id` - ID docente (riferimento esterno)
- `exam_date` - Data dell'esame
- `exam_time` - Orario dell'esame
- `classroom` - Aula
- `status` - Stato (enum: PLANNED, ONGOING, COMPLETED, CANCELLED)
- `max_enrollments` - Numero massimo iscrizioni
- `creation_date` - Data creazione
- `last_modified` - Data ultima modifica

#### ExamEnrollment (Iscrizione Esame)

- `id` - ID iscrizione
- `exam_id` - ID esame (FK)
- `student_id` - ID studente (riferimento esterno)
- `status` - Stato (enum: PENDING, APPROVED, REJECTED, COMPLETED)
- `enrollment_date` - Data iscrizione
- `notes` - Note studente
- `admin_notes` - Note amministratore

#### ExamGrade (Voto Esame)
- `id` - ID voto
- `enrollment_id` - ID iscrizione (FK)
- `grade` - Voto (18-30)
- `with_honors` - Con lode (boolean)
- `recording_date` - Data registrazione
- `notes` - Note docente
- `feedback` - Feedback dettagliato

## API REST

### Exams Endpoint

#### Gestione Esami (per Amministrativi)

```
#############################################
# Crea nuovo esame
# @func: createExam()
# @param: CreateExamRequest examRequest
# @return: ResponseEntity<ExamDTO>
#############################################
POST    /api/v1/exams

#############################################
# Lista tutti gli esami
# @func: getAllExams()
# @param: startDate, endDate, courseId, teacherId, page, size
# @return: ResponseEntity<List<ExamDTO>>
#############################################
GET     /api/v1/exams

#############################################
# Dettaglio singolo esame
# @func: getExamById()
# @param: Long id
# @return: ResponseEntity<ExamDTO>
#############################################
GET     /api/v1/exams/{id}

#############################################
# Aggiorna esame
# @func: updateExam()
# @param: Long id, UpdateExamRequest examRequest
# @return: ResponseEntity<ExamDTO>
#############################################
PUT     /api/v1/exams/{id}

#############################################
# Elimina esame
# @func: deleteExam()
# @param: Long id
# @return: ResponseEntity<Void>
#############################################
DELETE  /api/v1/exams/{id}

#############################################
# Esami per corso
# @func: getExamsByCourse()
# @param: Long courseId
# @return: ResponseEntity<List<ExamDTO>>
#############################################
GET     /api/v1/exams/course/{courseId}

#############################################
# Esami per docente
# @func: getExamsByTeacher()
# @param: Long teacherId
# @return: ResponseEntity<List<ExamDTO>>
#############################################
GET     /api/v1/exams/teacher/{teacherId}
```

#### Visualizzazione Calendario (per Tutti)

```
#############################################
# Calendario esami pubblico
# @func: getExamCalendar()
# @param: startDate, endDate, courseId, teacherId
# @return: ResponseEntity<List<ExamCalendarDTO>>
#############################################
GET     /api/v1/exams/calendar

#############################################
# Esami disponibili per iscrizione
# @func: getAvailableExams()
# @param: studentId (opzionale)
# @return: ResponseEntity<List<ExamDTO>>
#############################################
GET     /api/v1/exams/available
```

### Enrollments Endpoint

#### Gestione Iscrizioni (per Studenti)

```
#############################################
# Iscrizione a esame
# @func: enrollToExam()
# @param: Long examId, EnrollmentRequest request
# @return: ResponseEntity<EnrollmentDTO>
#############################################
POST    /api/v1/exams/{examId}/enroll

#############################################
# Le mie iscrizioni
# @func: getMyEnrollments()
# @param: status, page, size
# @return: ResponseEntity<List<EnrollmentDTO>>
#############################################
GET     /api/v1/enrollments/my

#############################################
# Dettaglio iscrizione
# @func: getEnrollmentById()
# @param: Long id
# @return: ResponseEntity<EnrollmentDTO>
#############################################
GET     /api/v1/enrollments/{id}

#############################################
# Cancella iscrizione
# @func: cancelEnrollment()
# @param: Long id
# @return: ResponseEntity<Void>
#############################################
DELETE  /api/v1/enrollments/{id}
```

#### Gestione Iscrizioni (per Amministrativi)

```
#############################################
# Iscrizioni per esame
# @func: getExamEnrollments()
# @param: Long examId, status, page, size
# @return: ResponseEntity<List<EnrollmentDTO>>
#############################################
GET     /api/v1/exams/{examId}/enrollments

#############################################
# Aggiorna stato iscrizione
# @func: updateEnrollmentStatus()
# @param: Long enrollmentId, UpdateStatusRequest request
# @return: ResponseEntity<EnrollmentDTO>
#############################################
PUT     /api/v1/enrollments/{enrollmentId}/status

#############################################
# Tutte le iscrizioni
# @func: getAllEnrollments()
# @param: examId, studentId, status, page, size
# @return: ResponseEntity<List<EnrollmentDTO>>
#############################################
GET     /api/v1/enrollments

#############################################
# Iscrizioni per studente
# @func: getStudentEnrollments()
# @param: Long studentId, status, page, size
# @return: ResponseEntity<List<EnrollmentDTO>>
#############################################
GET     /api/v1/enrollments/student/{studentId}
```

### Grades Endpoint

#### Gestione Voti (per Docenti)

```
#############################################
# Registra voto
# @func: recordGrade()
# @param: Long examId, CreateGradeRequest request
# @return: ResponseEntity<GradeDTO>
#############################################
POST    /api/v1/exams/{examId}/grades

#############################################
# Voti per esame
# @func: getExamGrades()
# @param: Long examId, minGrade, maxGrade, withHonors, page, size
# @return: ResponseEntity<List<GradeDTO>>
#############################################
GET     /api/v1/exams/{examId}/grades

#############################################
# Dettaglio voto
# @func: getGradeById()
# @param: Long id
# @return: ResponseEntity<GradeDTO>
#############################################
GET     /api/v1/grades/{id}

#############################################
# Aggiorna voto
# @func: updateGrade()
# @param: Long id, UpdateGradeRequest request
# @return: ResponseEntity<GradeDTO>
#############################################
PUT     /api/v1/grades/{id}

#############################################
# Elimina voto
# @func: deleteGrade()
# @param: Long id
# @return: ResponseEntity<Void>
#############################################
DELETE  /api/v1/grades/{id}
```

#### Visualizzazione Voti (per Studenti)

```
#############################################
# I miei voti
# @func: getMyGrades()
# @param: courseId, semester, academicYear, page, size
# @return: ResponseEntity<List<GradeDTO>>
#############################################
GET     /api/v1/grades/my

#############################################
# Voti per studente (admin/docente)
# @func: getStudentGrades()
# @param: Long studentId, courseId, semester, academicYear, page, size
# @return: ResponseEntity<List<GradeDTO>>
#############################################
GET     /api/v1/grades/student/{studentId}

#############################################
# Statistiche voti corso
# @func: getCourseGradeStatistics()
# @param: Long courseId
# @return: ResponseEntity<GradeStatisticsDTO>
#############################################
GET     /api/v1/grades/course/{courseId}/statistics
```

## Integrazione Microservizi Esterni

### Panoramica Generale

Il microservizio Gestione Esami interagisce con i seguenti microservizi:

- **Gestione Utenti e Ruoli:** Per verificare autorizzazioni e ottenere informazioni su studenti, docenti e amministrativi
- **Gestione Corsi:** Per ottenere informazioni sui corsi associati agli esami
- **Valutazione e Feedback:** Per sincronizzare i voti degli esami con le valutazioni
- **Comunicazioni e Notifiche:** Per inviare notifiche su iscrizioni, voti e modifiche esami

### RabbitMQ - Published Events

- `exam.created`: Quando viene creato un nuovo esame
- `exam.updated`: Quando un esame viene modificato
- `exam.deleted`: Quando un esame viene eliminato
- `exam.enrollment.requested`: Quando uno studente richiede iscrizione
- `exam.enrollment.approved`: Quando un'iscrizione viene approvata
- `exam.enrollment.rejected`: Quando un'iscrizione viene rifiutata
- `exam.grade.recorded`: Quando viene registrato un voto
- `exam.grade.updated`: Quando un voto viene modificato
- `exam.completed`: Quando un esame viene completato

### RabbitMQ - Consumed Events

- `user.updated`: Per aggiornare le informazioni degli utenti
- `course.updated`: Per aggiornare le informazioni dei corsi
- `course.deleted`: Per gestire la cancellazione di corsi con esami associati
- `teacher.course.assigned`: Per notificare nuove assegnazioni docenti

## Sicurezza e Autorizzazioni

L'accesso alle API è regolato da autorizzazioni basate sui ruoli:

- **ROLE_ADMINISTRATIVE:** Può pianificare, modificare e cancellare esami, gestire tutte le iscrizioni, visualizzare tutti i voti e generare statistiche
- **ROLE_TEACHER:** Può registrare e modificare voti per i propri corsi, visualizzare iscrizioni e voti degli esami dei propri corsi
- **ROLE_STUDENT:** Può iscriversi agli esami disponibili, visualizzare il calendario esami, consultare le proprie iscrizioni e visualizzare i propri voti

### Controlli di Sicurezza Aggiuntivi

- Validazione delle date degli esami (non nel passato)
- Controllo dei limiti di iscrizione per esame
- Verifica dei prerequisiti per l'iscrizione agli esami
- Audit trail per tutte le operazioni sensibili (creazione/modifica voti)
- Rate limiting per le API di iscrizione per prevenire spam