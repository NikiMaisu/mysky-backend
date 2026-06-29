package ge.mysky.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WorkerUpdateRequest(
        @NotBlank String name,
        @Email String email,
        String phone,
        @Size(min = 6) String password) {
}
