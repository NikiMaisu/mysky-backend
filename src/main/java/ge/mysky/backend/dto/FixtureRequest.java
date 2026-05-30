package ge.mysky.backend.dto;

import ge.mysky.backend.domain.FixtureUnit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record FixtureRequest(
        @NotBlank String name,
        @NotNull FixtureUnit unit,
        @NotNull @PositiveOrZero BigDecimal cost,
        @NotNull @PositiveOrZero BigDecimal installTimeMinutes) {
}
