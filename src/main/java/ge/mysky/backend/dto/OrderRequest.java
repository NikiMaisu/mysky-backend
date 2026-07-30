package ge.mysky.backend.dto;

import ge.mysky.backend.domain.OrderStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record OrderRequest(
        Long orderNumber,
        @NotBlank String clientName,
        String clientPhone,
        String address,
        @NotNull OffsetDateTime startAt,
        OffsetDateTime finishAt,
        Long teamId,
        boolean graniteEnabled,
        @PositiveOrZero BigDecimal perimeter,
        @PositiveOrZero BigDecimal flatAddedValue,
        TimeUnit flatAddedUnit,
        boolean finishOverridden,
        boolean costOverridden,
        @PositiveOrZero BigDecimal totalCost,
        OrderStatus status,
        String notes,
        @Valid List<MaterialLine> materials,
        @Valid List<FixtureLine> fixtures,
        @Valid List<AddonLine> addons) {

    public record MaterialLine(
            @NotNull Long materialId,
            @NotNull @PositiveOrZero BigDecimal squareMeters) {}

    public record FixtureLine(
            @NotNull Long fixtureId,
            @NotNull @PositiveOrZero BigDecimal quantity) {}

    public record AddonLine(
            @NotNull Long addonId,
            @NotNull @PositiveOrZero Integer quantity) {}
}
