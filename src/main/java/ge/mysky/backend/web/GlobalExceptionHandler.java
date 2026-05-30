package ge.mysky.backend.web;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException e) {
        return body(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(ConflictException e) {
        return body(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException e) {
        var fields = new LinkedHashMap<String, String>();
        e.getBindingResult().getFieldErrors()
                .forEach(err -> fields.putIfAbsent(err.getField(), err.getDefaultMessage()));
        var payload = base(HttpStatus.BAD_REQUEST, "Validation failed");
        payload.put("fields", fields);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(payload);
    }

    private ResponseEntity<Map<String, Object>> body(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(base(status, message));
    }

    private Map<String, Object> base(HttpStatus status, String message) {
        var payload = new LinkedHashMap<String, Object>();
        payload.put("timestamp", OffsetDateTime.now().toString());
        payload.put("status", status.value());
        payload.put("error", status.getReasonPhrase());
        payload.put("message", message);
        return payload;
    }
}
