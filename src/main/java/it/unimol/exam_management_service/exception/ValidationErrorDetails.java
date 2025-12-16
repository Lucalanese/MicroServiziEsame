package it.unimol.exam_management_service.exception;

import java.time.LocalDateTime;
import java.util.Map;

public class ValidationErrorDetails extends ErrorDetails {
    private Map<String, String> errors;

    public ValidationErrorDetails() {
        super();
    }

    public ValidationErrorDetails(LocalDateTime timestamp, int status, String error,
                                  String message, String path, Map<String, String> errors) {
        super(timestamp, status, error, message, path);
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }
}