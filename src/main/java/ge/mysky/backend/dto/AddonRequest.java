package ge.mysky.backend.dto;

import ge.mysky.backend.domain.AddonCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record AddonRequest(
        @NotBlank String name,
        AddonCategory category,
        @NotNull @PositiveOrZero BigDecimal cost,
        @NotNull @PositiveOrZero Integer installTimeMinutes) {
}
