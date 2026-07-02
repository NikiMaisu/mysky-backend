package ge.mysky.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import ge.mysky.backend.domain.AddonCategory;
import ge.mysky.backend.domain.FixtureUnit;
import ge.mysky.backend.domain.Order;
import ge.mysky.backend.domain.OrderAddon;
import ge.mysky.backend.domain.OrderFixture;
import ge.mysky.backend.domain.OrderMaterial;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class OrderCalculationServiceTest {

    private final OrderCalculationService calc = new OrderCalculationService();

    private Order baseOrder() {
        var o = new Order();
        var m = new OrderMaterial();
        m.setName("m");
        m.setUnitPricePerM2(new BigDecimal("75.00"));
        m.setUnitTimeMinutes(new BigDecimal("15.00"));
        m.setSquareMeters(new BigDecimal("20"));
        o.addMaterial(m);
        o.setGraniteEnabled(false);
        o.setFlatAddedMinutes(0);
        return o;
    }

    private static OrderMaterial firstMaterial(Order o) {
        return o.getMaterials().get(0);
    }

    private OrderFixture fixture(String cost, String time, String qty) {
        var f = new OrderFixture();
        f.setName("f");
        f.setUnit(FixtureUnit.PER_UNIT);
        f.setUnitCost(new BigDecimal(cost));
        f.setUnitTimeMinutes(new BigDecimal(time));
        f.setQuantity(new BigDecimal(qty));
        return f;
    }

    private OrderAddon addon(String cost, int time, int qty) {
        var a = new OrderAddon();
        a.setName("a");
        a.setCategory(AddonCategory.OTHER);
        a.setUnitCost(new BigDecimal(cost));
        a.setUnitTimeMinutes(time);
        a.setQuantity(qty);
        return a;
    }

    @Test
    void materialOnly() {
        var totals = calc.compute(baseOrder());
        assertThat(totals.cost()).isEqualByComparingTo("1500.00"); // 75 * 20
        assertThat(totals.minutes()).isEqualTo(300); // 15 * 20
    }

    @Test
    void zeroSquareMeters() {
        var o = baseOrder();
        firstMaterial(o).setSquareMeters(BigDecimal.ZERO);
        o.setFlatAddedMinutes(45);
        var totals = calc.compute(o);
        assertThat(totals.cost()).isEqualByComparingTo("0.00");
        assertThat(totals.minutes()).isEqualTo(45); // only flat time
    }

    @Test
    void graniteAddsPerimeterCostAndTime() {
        var o = baseOrder();
        o.setGraniteEnabled(true);
        o.setPerimeter(new BigDecimal("18"));
        o.setGranitePricePerMeter(new BigDecimal("20"));
        o.setGraniteTimePerMeterMinutes(new BigDecimal("3"));
        var totals = calc.compute(o);
        assertThat(totals.cost()).isEqualByComparingTo("1860.00"); // 1500 + 360
        assertThat(totals.minutes()).isEqualTo(354); // 300 + 54
    }

    @Test
    void fullOrderWithFixturesAddonsGraniteAndFlat() {
        var o = baseOrder();
        o.setGraniteEnabled(true);
        o.setPerimeter(new BigDecimal("18"));
        o.setGranitePricePerMeter(new BigDecimal("20"));
        o.setGraniteTimePerMeterMinutes(new BigDecimal("3"));
        o.addFixture(fixture("8", "5", "10")); // +80 cost, +50 min
        o.addAddon(addon("45", 30, 2)); // +90 cost, +60 min
        o.setFlatAddedMinutes(15);
        var totals = calc.compute(o);
        assertThat(totals.cost()).isEqualByComparingTo("2030.00"); // 1500+360+80+90
        assertThat(totals.minutes()).isEqualTo(479); // 300+54+50+60+15
    }

    @Test
    void multipleFixtures() {
        var o = baseOrder();
        firstMaterial(o).setSquareMeters(BigDecimal.ZERO);
        o.addFixture(fixture("10", "4", "3")); // 30 cost, 12 min
        o.addFixture(fixture("5", "2", "2")); // 10 cost, 4 min
        var totals = calc.compute(o);
        assertThat(totals.cost()).isEqualByComparingTo("40.00");
        assertThat(totals.minutes()).isEqualTo(16);
    }

    @Test
    void fractionalMinutesRoundHalfUp() {
        var o = baseOrder();
        var m = firstMaterial(o);
        m.setUnitTimeMinutes(new BigDecimal("2.5"));
        m.setSquareMeters(new BigDecimal("3")); // 7.5 min -> rounds to 8
        m.setUnitPricePerM2(BigDecimal.ZERO);
        var totals = calc.compute(o);
        assertThat(totals.minutes()).isEqualTo(8);
    }
}
