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
        @NotBlank String clientName,
        String clientPhone,
        String address,
        @NotNull OffsetDateTime startAt,
        OffsetDateTime finishAt,
        Long teamId,
        @NotNull Long materialId,
        @NotNull @PositiveOrZero BigDecimal squareMeters,
        boolean graniteEnabled,
        @PositiveOrZero BigDecimal perimeter,
        @PositiveOrZero Integer flatAddedMinutes,
        OrderStatus status,
        String notes,
        @Valid List<FixtureLine> fixtures,
        @Valid List<AddonLine> addons) {

    public record FixtureLine(
            @NotNull Long fixtureId,
            @NotNull @PositiveOrZero BigDecimal quantity) {}

    public record AddonLine(
            @NotNull Long addonId,
            @NotNull @PositiveOrZero Integer quantity) {}
}
