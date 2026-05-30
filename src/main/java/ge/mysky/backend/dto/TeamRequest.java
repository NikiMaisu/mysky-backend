package ge.mysky.backend.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record TeamRequest(
        @NotBlank String name,
        List<Long> memberIds) {
}
