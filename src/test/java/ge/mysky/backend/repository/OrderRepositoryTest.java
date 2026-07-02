package ge.mysky.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import ge.mysky.backend.AbstractIntegrationTest;
import ge.mysky.backend.domain.Order;
import ge.mysky.backend.domain.OrderMaterial;
import ge.mysky.backend.domain.OrderStatus;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class OrderRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    OrderRepository orders;

    private Order minimalOrder(String client, OrderStatus status, OffsetDateTime start) {
        var o = new Order();
        o.setOrderNumber(orders.nextOrderNumber());
        o.setClientName(client);
        o.setStartAt(start);
        o.setFinishAt(start.plusHours(2));
        var m = new OrderMaterial();
        m.setName("Snapshot material");
        m.setUnitPricePerM2(new BigDecimal("10.00"));
        m.setUnitTimeMinutes(new BigDecimal("5.00"));
        m.setSquareMeters(new BigDecimal("10"));
        o.addMaterial(m);
        o.setGraniteEnabled(false);
        o.setFlatAddedMinutes(0);
        o.setTotalMinutes(50);
        o.setTotalCost(new BigDecimal("100.00"));
        o.setStatus(status);
        return o;
    }

    @Test
    void orderNumberIsGeneratedBySequence() {
        var saved = orders.save(minimalOrder("Seq Client", OrderStatus.QUOTED, OffsetDateTime.now()));
        assertThat(saved.getOrderNumber()).isNotNull();
        assertThat(saved.getOrderNumber()).isGreaterThanOrEqualTo(1001L);
    }

    @Test
    void specificationFiltersByStatus() {
        var marker = OffsetDateTime.parse("2099-03-03T10:00:00+04:00");
        orders.save(minimalOrder("Spec Done", OrderStatus.DONE, marker));
        orders.save(minimalOrder("Spec Quoted", OrderStatus.QUOTED, marker));

        Specification<Order> spec = (root, q, cb) -> {
            var preds = new ArrayList<Predicate>();
            preds.add(cb.equal(root.get("status"), OrderStatus.DONE));
            preds.add(cb.greaterThanOrEqualTo(root.get("startAt"), marker));
            return cb.and(preds.toArray(new Predicate[0]));
        };
        var result = orders.findAll(spec);
        assertThat(result).isNotEmpty();
        assertThat(result).allMatch(o -> o.getStatus() == OrderStatus.DONE);
        assertThat(result).extracting(Order::getClientName).contains("Spec Done").doesNotContain("Spec Quoted");
    }
}
