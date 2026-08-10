package ge.mysky.backend.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Mock
    private MethodArgumentNotValidException validationException;

    @Mock
    private BindingResult bindingResult;

    @Test
    void notFoundMapsTo404WithMessage() {
        var response = handler.handleNotFound(new NotFoundException("Material 9 not found"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("status", 404);
        assertThat(response.getBody()).containsEntry("message", "Material 9 not found");
    }

    @Test
    void conflictMapsTo409WithMessage() {
        var response = handler.handleConflict(new ConflictException("A user with email a@b.com already exists"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).containsEntry("status", 409);
    }

    @Test
    void validationMapsTo400WithFieldErrors() {
        var fieldError = new FieldError("materialRequest", "name", "must not be blank");
        when(validationException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        var response = handler.handleValidation(validationException);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("message", "Validation failed");
        @SuppressWarnings("unchecked")
        var fields = (java.util.Map<String, String>) response.getBody().get("fields");
        assertThat(fields).containsEntry("name", "must not be blank");
    }

    @Test
    void validationKeepsFirstErrorPerField() {
        var first = new FieldError("materialRequest", "name", "must not be blank");
        var second = new FieldError("materialRequest", "name", "must be at most 120 chars");
        when(validationException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(first, second));

        var response = handler.handleValidation(validationException);

        @SuppressWarnings("unchecked")
        var fields = (java.util.Map<String, String>) response.getBody().get("fields");
        assertThat(fields).containsEntry("name", "must not be blank");
    }
}
