# Microservizio Gestione Esami

## Descrizione

Il microservizio che riguarda la Gestione Esami è responsabile delle operazioni legate appunto alla gestione degli esami all’interno della piattaforma universitaria. Essa fornisce funzionalità per visualizzare il calendario degli esami, l’iscrizione degli studenti, la registrazione dei voti da parte dei docenti e la pianificazione degli esami da parte dell’amministrazione.

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

- `GET /api/v1/exams`  
  Recupera il calendario degli esami disponibili (per tutti).

- `GET /api/v1/exams/{id}`  
  Recupera i dettagli di un singolo esame (per tutti).

- `POST /api/v1/exams`  
  Pianifica un nuovo esame (solo amministratori).

- `PUT /api/v1/exams/{id}`  
  Modifica i dettagli di un esame (solo amministratori).

- `DELETE /api/v1/exams/{id}`  
  Elimina un esame pianificato (solo amministratori).

### Iscrizioni Esami Endpoint

- `POST /api/v1/exams/{id}/enroll`  
  Iscrizione dello studente a un esame.

- `GET /api/v1/exams/{id}/enrollments`  
  Recupera le iscrizioni per un esame (per docenti e amministratori).

- `PUT /api/v1/enrollments/{enrollmentId}/status`  
  Aggiorna lo stato di un’iscrizione (approvata/rifiutata) (per amministratori).

### Voti Esami Endpoint

- `POST /api/v1/exams/{id}/grades`  
  Registra un voto per uno studente in un esame (per docenti).

- `GET /api/v1/exams/{id}/grades`  
  Visualizza tutti i voti per un esame (docenti e amministratori).

- `GET /api/v1/students/{id}/grades`  
  Visualizza i voti di uno studente per tutti gli esami (perstudenti).

## Integrazione con altri Microservizi

Il microservizio Gestione Esami interagisce con  **Gestione Utenti e Ruoli** per l'autenticazione, l'autorizzazione e per recuperare le  informazioni sugli utenti (studenti, docenti, amministrativi) poi con **Gestione Corsi** per ottenere informazioni sui corsi associati agli esami e con **Valutazione e Feedback** per sincronizzare i voti degli esami con le valutazioni.  

## Eventi (RabbitMQ)

**Eventi pubblicati:**

- `exam.created` — Quando un esame viene pianificato.  
- `exam.updated` — Quando un esame viene modificato.  
- `exam.deleted` — Quando un esame viene cancellato.  
- `exam.enrollment.requested` — Quando uno studente richiede iscrizione a un esame.  
- `exam.enrollment.status_changed` — Quando lo stato dell’iscrizione cambia (approvata o rifiutata).  
- `exam.grade.recorded` — Quando un voto viene registrato.

**Eventi consumati:**

- `user.updated` — Per aggiornare le informazioni degli utenti coinvolti negli esami.  
- `course.updated` — Per aggiornare le informazioni dei corsi associati agli esami.

## Sicurezza e Autorizzazioni

L’accesso alle API è regolato da autorizzazioni basate sui ruoli:

- **ROLE_ADMINISTRATIVE:** Può pianificare, modificare e cancellare esami, gestire iscrizioni e visualizzare voti di tutti gli studenti.  
- **ROLE_TEACHER:** Può registrare voti e visualizzare iscrizioni e voti degli esami dei propri corsi.  
- **ROLE_STUDENT:** Può iscriversi agli esami, visualizzare il calendario esami e i propri risultati.
