package ge.mysky.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

public record GraniteConfigRequest(
        @NotNull @PositiveOrZero BigDecimal pricePerMeter,
        @NotNull @PositiveOrZero BigDecimal timePerMeterMinutes) {
}
