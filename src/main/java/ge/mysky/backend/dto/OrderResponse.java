package ge.mysky.backend.dto;

import ge.mysky.backend.domain.AddonCategory;
import ge.mysky.backend.domain.FixtureUnit;
import ge.mysky.backend.domain.Order;
import ge.mysky.backend.domain.OrderAddon;
import ge.mysky.backend.domain.OrderFixture;
import ge.mysky.backend.domain.OrderStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        Long orderNumber,
        String clientName,
        String clientPhone,
        String address,
        OffsetDateTime startAt,
        OffsetDateTime finishAt,
        Long teamId,
        String teamName,
        Long materialId,
        String materialName,
        BigDecimal materialPricePerM2,
        BigDecimal materialTimePerM2Minutes,
        BigDecimal squareMeters,
        boolean graniteEnabled,
        BigDecimal perimeter,
        BigDecimal granitePricePerMeter,
        BigDecimal graniteTimePerMeterMinutes,
        int flatAddedMinutes,
        int totalMinutes,
        BigDecimal totalCost,
        boolean finishOverridden,
        OrderStatus status,
        String notes,
        List<FixtureLine> fixtures,
        List<AddonLine> addons) {

    public record FixtureLine(
            Long fixtureId,
            String name,
            FixtureUnit unit,
            BigDecimal unitCost,
            BigDecimal unitTimeMinutes,
            BigDecimal quantity,
            BigDecimal lineCost,
            BigDecimal lineTimeMinutes) {

        static FixtureLine from(OrderFixture f) {
            var qty = f.getQuantity();
            return new FixtureLine(
                    f.getFixtureId(), f.getName(), f.getUnit(), f.getUnitCost(), f.getUnitTimeMinutes(), qty,
                    f.getUnitCost().multiply(qty), f.getUnitTimeMinutes().multiply(qty));
        }
    }

    public record AddonLine(
            Long addonId,
            String name,
            AddonCategory category,
            BigDecimal unitCost,
            Integer unitTimeMinutes,
            Integer quantity,
            BigDecimal lineCost,
            Integer lineTimeMinutes) {

        static AddonLine from(OrderAddon a) {
            int qty = a.getQuantity();
            return new AddonLine(
                    a.getAddonId(), a.getName(), a.getCategory(), a.getUnitCost(), a.getUnitTimeMinutes(), qty,
                    a.getUnitCost().multiply(BigDecimal.valueOf(qty)), a.getUnitTimeMinutes() * qty);
        }
    }

    public static OrderResponse from(Order o) {
        return new OrderResponse(
                o.getId(), o.getOrderNumber(), o.getClientName(), o.getClientPhone(), o.getAddress(),
                o.getStartAt(), o.getFinishAt(), o.getTeamId(), o.getTeamName(),
                o.getMaterialId(), o.getMaterialName(), o.getMaterialPricePerM2(), o.getMaterialTimePerM2Minutes(),
                o.getSquareMeters(), o.isGraniteEnabled(), o.getPerimeter(),
                o.getGranitePricePerMeter(), o.getGraniteTimePerMeterMinutes(),
                o.getFlatAddedMinutes(), o.getTotalMinutes(), o.getTotalCost(), o.isFinishOverridden(), o.getStatus(), o.getNotes(),
                o.getFixtures().stream().map(FixtureLine::from).toList(),
                o.getAddons().stream().map(AddonLine::from).toList());
    }
}
