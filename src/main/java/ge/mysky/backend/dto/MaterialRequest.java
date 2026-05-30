package ge.mysky.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record MaterialRequest(
        @NotBlank String name,
        @NotNull @PositiveOrZero BigDecimal pricePerM2,
        @NotNull @PositiveOrZero BigDecimal timePerM2Minutes) {
}
