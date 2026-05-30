package ge.mysky.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WorkerCreateRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6) String password) {
}
