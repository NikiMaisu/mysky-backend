package ge.mysky.backend.service;

import ge.mysky.backend.domain.Order;
import ge.mysky.backend.domain.OrderAddon;
import ge.mysky.backend.domain.OrderFixture;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

/**
 * Computes total time and cost from an order's snapshot values (not live config).
 *
 *  time = material.timePerM2 * m²
 *       + (granite ? perimeter * granite.timePerMeter : 0)
 *       + Σ fixture.unitTime * qty
 *       + Σ addon.unitTime  * qty
 *       + flatAddedMinutes
 *
 *  cost = material.pricePerM2 * m²
 *       + (granite ? perimeter * granite.pricePerMeter : 0)
 *       + Σ fixture.unitCost * qty
 *       + Σ addon.unitCost  * qty
 */
@Service
public class OrderCalculationService {

    public record Totals(int minutes, BigDecimal cost) {}

    public Totals compute(Order o) {
        var sqm = nz(o.getSquareMeters());

        BigDecimal minutes = nz(o.getMaterialTimePerM2Minutes()).multiply(sqm);
        BigDecimal cost = nz(o.getMaterialPricePerM2()).multiply(sqm);

        if (o.isGraniteEnabled()) {
            var perimeter = nz(o.getPerimeter());
            minutes = minutes.add(nz(o.getGraniteTimePerMeterMinutes()).multiply(perimeter));
            cost = cost.add(nz(o.getGranitePricePerMeter()).multiply(perimeter));
        }

        for (OrderFixture f : o.getFixtures()) {
            var qty = nz(f.getQuantity());
            minutes = minutes.add(nz(f.getUnitTimeMinutes()).multiply(qty));
            cost = cost.add(nz(f.getUnitCost()).multiply(qty));
        }

        for (OrderAddon a : o.getAddons()) {
            var qty = BigDecimal.valueOf(a.getQuantity() == null ? 0 : a.getQuantity());
            minutes = minutes.add(BigDecimal.valueOf(a.getUnitTimeMinutes() == null ? 0 : a.getUnitTimeMinutes()).multiply(qty));
            cost = cost.add(nz(a.getUnitCost()).multiply(qty));
        }

        minutes = minutes.add(BigDecimal.valueOf(o.getFlatAddedMinutes()));

        int totalMinutes = minutes.setScale(0, RoundingMode.HALF_UP).intValueExact();
        return new Totals(totalMinutes, cost.setScale(2, RoundingMode.HALF_UP));
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
