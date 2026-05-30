package ge.mysky.backend.dto;

import jakarta.validation.constraints.NotNull;

public record TeamMemberRequest(
        @NotNull Long workerId) {
}
