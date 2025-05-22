# Microservizio Gestione Esami

## Descrizione

Il microservizio che riguarda la Gestione Esami è responsabile delle operazioni legate appunto alla gestione degli esami all'interno della piattaforma universitaria. Essa fornisce funzionalità per visualizzare il calendario degli esami, l'iscrizione degli studenti, la registrazione dei voti da parte dei docenti e la pianificazione degli esami da parte dell'amministrazione.

Le funzionalità principali sono:

- Visualizzazione del calendario esami (tutti gli utenti)
- Iscrizione agli esami (studenti)
- Registrazione voti (docenti)
- Pianificazione esami (amministrativi)
- Visualizzazione risultati esami (studenti)

## Tech Stack

- Framework: SpringBoot
- Message Broker: RabbitMQ
- Database: MySQL
- Containerization: Docker (non presente su questa repo)
- Orchestration: Kubernetes (non presente su questa repo)
- API Documentation: Swagger/OpenAPI 3.0

## Modello dei dati (descrizione)

- **Esame**: id, id_corso, data, ora, aula, stato (pianificato, concluso)
- **IscrizioneEsame**: id, id_esame, id_studente, stato (in attesa, approvata, rifiutata), data_iscrizione
- **VotoEsame**: id, id_iscrizione, voto, lode, data_registrazione, note
- **CalendarioEsami**: collezione di esami con filtri per periodo, corso, docente

## API REST

### Esami Endpoint

#### `GET /api/v1/exams`

**Funzione:** `getAllExams()`

**Descrizione:** Recupera il calendario degli esami disponibili. Permette la visualizzazione di tutti gli esami pianificati nel sistema con possibilità di filtraggio per periodo, corso o docente.

**Parametri:**

- `startDate` - Data di inizio del periodo per filtrare gli esami (formato: yyyy-MM-dd, opzionale)
- `endDate` - Data di fine del periodo per filtrare gli esami (formato: yyyy-MM-dd, opzionale)
- `courseId` - ID del corso per filtrare gli esami di un corso specifico (opzionale)
- `teacherId` - ID del docente per filtrare gli esami di un docente specifico (opzionale)
- `page` - Numero della pagina per la paginazione (default: 0, opzionale)
- `size` - Dimensione della pagina per la paginazione (default: 20, opzionale)

**Return:** `List<ExamDTO>` - Lista degli esami che soddisfano i criteri di ricerca con informazioni complete su data, ora, aula, corso e stato

---

#### `GET /api/v1/exams/{id}`

**Funzione:** `getExamById()`

**Descrizione:** Recupera i dettagli completi di un singolo esame specifico identificato dall'ID fornito.

**Parametri:**

- `id` - ID univoco dell'esame da recuperare (Long, required)

**Return:** `ExamDTO` - Oggetto contenente tutti i dettagli dell'esame (id, corso, data, ora, aula, stato, informazioni docente)

---

#### `POST /api/v1/exams`

**Funzione:** `createExam()`

**Descrizione:** Pianifica un nuovo esame nel sistema. Permette agli amministratori di creare un nuovo esame con tutti i dettagli necessari.

**Parametri:**

- `examRequest` - Oggetto contenente i dati del nuovo esame da creare (CreateExamRequest, required)
  - `courseId` (Long) - ID del corso associato all'esame
  - `examDate` (LocalDate) - Data dell'esame
  - `examTime` (LocalTime) - Orario dell'esame
  - `classroom` (String) - Aula dove si terrà l'esame
  - `maxEnrollments` (Integer) - Numero massimo di iscrizioni consentite

**Return:** `ExamDTO` - Oggetto rappresentante l'esame appena creato con ID assegnato dal sistema

---

#### `PUT /api/v1/exams/{id}`

**Funzione:** `updateExam()`

**Descrizione:** Modifica i dettagli di un esame esistente. Solo gli amministratori possono aggiornare le informazioni di un esame pianificato.

**Parametri:**

- `id` - ID dell'esame da modificare (Long, required)
- `examRequest` - Oggetto contenente i nuovi dati dell'esame (UpdateExamRequest, required)
  - `courseId` (Long, opzionale) - Nuovo ID del corso
  - `examDate` (LocalDate, opzionale) - Nuova data dell'esame
  - `examTime` (LocalTime, opzionale) - Nuovo orario dell'esame
  - `classroom` (String, opzionale) - Nuova aula
  - `status` (ExamStatus, opzionale) - Nuovo stato dell'esame
  - `maxEnrollments` (Integer, opzionale) - Nuovo numero massimo di iscrizioni

**Return:** `ExamDTO` - Oggetto rappresentante l'esame aggiornato con le modifiche applicate

---

#### `DELETE /api/v1/exams/{id}`

**Funzione:** `deleteExam()`

**Descrizione:** Elimina definitivamente un esame pianificato dal sistema. Operazione consentita solo agli amministratori e solo per esami non ancora conclusi.

**Parametri:**

- `id` - ID dell'esame da eliminare (Long, required)

**Return:** `void` - Nessun contenuto restituito, solo status HTTP 204 (No Content) in caso di successo

---

### Iscrizioni Esami Endpoint

#### `POST /api/v1/exams/{id}/enroll`

**Funzione:** `enrollToExam()`

**Descrizione:** Permette a uno studente di iscriversi a un esame specifico. Verifica automaticamente i prerequisiti e la disponibilità di posti.


**Parametri:**

- `id` - ID dell'esame a cui iscriversi (Long, required)
- `enrollmentRequest` - Oggetto contenente i dati dell'iscrizione (EnrollmentRequest, required)
  - `studentId` (Long) - ID dello studente che si vuole iscrivere
  - `notes` (String, opzionale) - Note aggiuntive per l'iscrizione

**Return:** `EnrollmentDTO` - Oggetto rappresentante l'iscrizione creata con stato iniziale "in attesa"

---

#### `GET /api/v1/exams/{id}/enrollments`

**Funzione:** `getExamEnrollments()`

**Descrizione:** Recupera tutte le iscrizioni per un esame specifico. Accessibile a docenti del corso e amministratori.

**Parametri:**

- `id` - ID dell'esame di cui recuperare le iscrizioni (Long, required)
- `status` - Filtro per stato dell'iscrizione (in_attesa, approvata, rifiutata, opzionale)
- `page` - Numero della pagina per la paginazione (default: 0, opzionale)
- `size` - Dimensione della pagina per la paginazione (default: 20, opzionale)

**Return:** `List<EnrollmentDTO>` - Lista delle iscrizioni all'esame con informazioni su studente, stato e data iscrizione

---

#### `PUT /api/v1/enrollments/{enrollmentId}/status`

**Funzione:** `updateEnrollmentStatus()`

**Descrizione:** Aggiorna lo stato di un'iscrizione a un esame (approvazione o rifiuto). Operazione riservata agli amministratori.

**Parametri:**

- `enrollmentId` - ID dell'iscrizione da aggiornare (Long, required)
- `statusRequest` - Oggetto contenente il nuovo stato (UpdateEnrollmentStatusRequest, required)
  - `status` (EnrollmentStatus) - Nuovo stato dell'iscrizione (approvata, rifiutata)
  - `adminNotes` (String, opzionale) - Note dell'amministratore sulla decisione

**Return:** `EnrollmentDTO` - Oggetto rappresentante l'iscrizione con lo stato aggiornato

---

### Voti Esami Endpoint

#### `POST /api/v1/exams/{id}/grades`

**Funzione:** `recordGrade()`

**Descrizione:** Registra un voto per uno studente in un esame specifico. Operazione consentita solo ai docenti del corso associato all'esame.

**Parametri:**

- `id` - ID dell'esame per cui registrare il voto (Long, required)
- `gradeRequest` - Oggetto contenente i dati del voto (CreateGradeRequest, required)
  - `enrollmentId` (Long) - ID dell'iscrizione dello studente
  - `grade` (Integer) - Voto numerico (18-30)
  - `withHonors` (Boolean) - Indica se il voto è con lode
  - `notes` (String, opzionale) - Note aggiuntive del docente
  - `feedback` (String, opzionale) - Feedback dettagliato per lo studente

**Return:** `GradeDTO` - Oggetto rappresentante il voto registrato con data e dettagli completi

---

#### `GET /api/v1/exams/{id}/grades`

**Funzione:** `getExamGrades()`

**Descrizione:** Visualizza tutti i voti registrati per un esame specifico. Accessibile a docenti del corso e amministratori.

**Parametri:**

- `id` - ID dell'esame di cui visualizzare i voti (Long, required)
- `minGrade` - Voto minimo per filtrare i risultati (opzionale)
- `maxGrade` - Voto massimo per filtrare i risultati (opzionale)
- `withHonors` - Filtra solo i voti con lode (Boolean, opzionale)
- `page` - Numero della pagina per la paginazione (default: 0, opzionale)
- `size` - Dimensione della pagina per la paginazione (default: 20, opzionale)

**Return:** `List<GradeDTO>` - Lista di tutti i voti dell'esame con informazioni su studente, voto e feedback

---

#### `GET /api/v1/students/{id}/grades`

**Funzione:** `getStudentGrades()`

**Descrizione:** Visualizza tutti i voti di uno studente specifico per tutti gli esami sostenuti. Accessibile allo studente stesso e agli amministratori.

**Parametri:**

- `id` - ID dello studente di cui visualizzare i voti (Long, required)
- `courseId` - Filtro per corso specifico (opzionale)
- `semester` - Filtro per semestre (String, opzionale)
- `academicYear` - Filtro per anno accademico (String, opzionale)
- `minGrade` - Voto minimo per filtrare i risultati (opzionale)
- `page` - Numero della pagina per la paginazione (default: 0, opzionale)
- `size` - Dimensione della pagina per la paginazione (default: 20, opzionale)

**Return:** `List<StudentGradeDTO>` - Lista completa dei voti dello studente con informazioni su corso, esame e performance

## Integrazione con altri Microservizi

Il microservizio Gestione Esami interagisce con **Gestione Utenti e Ruoli** per l'autenticazione, l'autorizzazione e per recuperare le informazioni sugli utenti (studenti, docenti, amministrativi) poi con **Gestione Corsi** per ottenere informazioni sui corsi associati agli esami e con **Valutazione e Feedback** per sincronizzare i voti degli esami con le valutazioni.

## Eventi (RabbitMQ)

**Eventi pubblicati:**

- `exam.created` — Quando un esame viene pianificato.
- `exam.updated` — Quando un esame viene modificato.
- `exam.deleted` — Quando un esame viene cancellato.
- `exam.enrollment.requested` — Quando uno studente richiede iscrizione a un esame.
- `exam.enrollment.status_changed` — Quando lo stato dell'iscrizione cambia (approvata o rifiutata).
- `exam.grade.recorded` — Quando un voto viene registrato.

**Eventi consumati:**

- `user.updated` — Per aggiornare le informazioni degli utenti coinvolti negli esami.
- `course.updated` — Per aggiornare le informazioni dei corsi associati agli esami.

## Sicurezza e Autorizzazioni

L'accesso alle API è regolato da autorizzazioni basate sui ruoli:

- **ROLE_ADMINISTRATIVE:** Può pianificare, modificare e cancellare esami, gestire iscrizioni e visualizzare voti di tutti gli studenti.
- **ROLE_TEACHER:** Può registrare voti e visualizzare iscrizioni e voti degli esami dei propri corsi.
- **ROLE_STUDENT:** Può iscriversi agli esami, visualizzare il calendario esami e i propri risultati.