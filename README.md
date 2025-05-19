# Introduzione

## Scopo del microservizio

Il microservizio di **Gestione Esami** ha il compito di gestire tutte le operazioni relative agli esami universitari, tra cui la pianificazione, la registrazione, l'iscrizione da parte degli studenti e la pubblicazione degli esiti. Questo microservizio è un componente fondamentale della piattaforma universitaria e permette l’interazione tra studenti, docenti e segreteria.

### Funzionalità principali

Il microservizio offre le seguenti funzionalità:

- Creazione e gestione degli appelli d’esame
- Iscrizione e cancellazione da parte degli studenti
- Inserimento e modifica degli esiti d’esame da parte dei docenti
- Consultazione degli appelli ed esiti da parte degli studenti
- Notifica di eventi importanti tramite RabbitMQ (es. pubblicazione esiti)

### Architettura

### Tecnologie utilizzate

- **Spring Boot**: Framework per lo sviluppo del microservizio
- **PostgreSQL**: Database relazionale per la persistenza dei dati
- **RabbitMQ**: Sistema di messaggistica per la comunicazione asincrona

### Schema di base del sistema

Il microservizio adotta un’architettura a strati ispirata al pattern MVC con un livello service per la business logic:

- **Model**: Entità come Exam, ExamSession, ExamResult
- **Repository**: Interfacce che accedono al database tramite Spring Data JPA
- **Service**: Logica di business legata alla gestione degli esami
- **Controller**: API REST esposte verso il mondo esterno
- **Messaging**: Gestione eventi tramite RabbitMQ

### API

### Endpoint disponibili

#### API per la gestione degli appelli

- `POST /api/exams/sessions` –Crea un nuovo appello d’esame
- `GET /api/exams/sessions` – Elenca tutti gli appelli
- `GET /api/exams/sessions/{sessionId}` – Visualizza i dettagli di un appello
- `DELETE /api/exams/sessions/{sessionId}` – Cancella un appello

### API per l'iscrizione agli esami

- `POST /api/exams/sessions/{sessionId}/enroll` – Iscrive uno studente all’esame
- `DELETE /api/exams/sessions/{sessionId}/unenroll/{studentId}` – Annulla l’iscrizione

### API per la gestione degli esiti

- `POST /api/exams/results` – Registra un nuovo esito d’esame
- `PUT /api/exams/results/{resultId}` – Modifica un esito
- `GET /api/exams/results/student/{studentId}` – Elenca gli esiti di uno studente
- `GET /api/exams/results/session/{sessionId}` – Elenca tutti gli esiti per un appello

### Esempi di richieste e risposte

### Iscrizione a un appello

**Richiesta:**

``` http
POST /api/exams/sessions/123/enroll
Content-Type: application/json

{
  "studentId": "s456"
}
```

**Risposta:**

``` json
Status: 200 OK
Content-Type: application/json

{
  "message": "Iscrizione completata con successo"
}
```

## Database

### Struttura tabelle principali

#### Tabelle exam_session

``` sql
CREATE TABLE exam_sessions (
  id SERIAL PRIMARY KEY,
  course_id VARCHAR NOT NULL,
  date DATE NOT NULL,
  location VARCHAR,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### Tabella exam_enrollments

```sql
CREATE TABLE exam_enrollments (
  id SERIAL PRIMARY KEY,
  session_id INT REFERENCES exam_sessions(id),
  student_id VARCHAR NOT NULL,
  enrolled_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### Tabella exam_results

```sql
CREATE TABLE exam_results (
  id SERIAL PRIMARY KEY,
  session_id INT REFERENCES exam_sessions(id),
  student_id VARCHAR NOT NULL,
  grade INT,
  published_at TIMESTAMP,
  status VARCHAR -- E.g., "VERBALIZZATO", "DA_ACCETTARE", etc.
);

```

## Integrazione

### Come interagire con il servizio

Il microservizio espone API REST per consentire a studenti, docenti e personale amministrativo di gestire tutte le attività relative agli esami. Eventuali modifiche rilevanti (es. inserimento o modifica di un esito) possono generare eventi RabbitMQ per avvisare altri servizi (es. notifiche).

### Esempio di uso di RabbitMQ

#### Configurazione RabbitMQ

``` java
@Configuration
public class RabbitMQExamConfig {
    public static final String EXAM_EVENT_QUEUE = "exam.event.queue";
    public static final String EXAM_EXCHANGE = "exam.exchange";
    public static final String EXAM_ROUTING_KEY = "exam.event.routingkey";

    @Bean
    public Queue examQueue() {
        return new Queue(EXAM_EVENT_QUEUE, false);
    }

    @Bean
    public DirectExchange examExchange() {
        return new DirectExchange(EXAM_EXCHANGE);
    }

    @Bean
    public Binding binding(Queue examQueue, DirectExchange examExchange) {
        return BindingBuilder.bind(examQueue).to(examExchange).with(EXAM_ROUTING_KEY);
    }
}
```

#### Evento di pubblicazione esito

``` java
@Component
public class ExamEventPublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishGradeNotification(String studentId, int grade, String courseId) {
        Map<String, Object> event = new HashMap<>();
        event.put("studentId", studentId);
        event.put("grade", grade);
        event.put("courseId", courseId);
        event.put("type", "GRADE_PUBLISHED");

        rabbitTemplate.convertAndSend(
            RabbitMQExamConfig.EXAM_EXCHANGE,
            RabbitMQExamConfig.EXAM_ROUTING_KEY,
            event
        );
    }
}
```

## Conclusioni

Il microservizio **Gestione Esami** centralizza tutte le operazioni legate agli appelli e agli esiti d’esame, integrandosi perfettamente con altri microservizi del sistema universitario. Grazie all’utilizzo di PostgreSQL e RabbitMQ, il sistema garantisce sia affidabilità nella persistenza dei dati che flessibilità nella comunicazione asincrona, migliorando l’esperienza utente e l’interoperabilità tra i moduli.