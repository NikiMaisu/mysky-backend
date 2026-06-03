package ge.mysky.backend.service;

import ge.mysky.backend.domain.Order;
import ge.mysky.backend.domain.OrderAddon;
import ge.mysky.backend.domain.OrderFixture;
import ge.mysky.backend.domain.OrderStatus;
import ge.mysky.backend.dto.OrderRequest;
import ge.mysky.backend.dto.OrderResponse;
import ge.mysky.backend.repository.AddonInstanceRepository;
import ge.mysky.backend.repository.FixtureRepository;
import ge.mysky.backend.repository.MaterialRepository;
import ge.mysky.backend.repository.OrderRepository;
import ge.mysky.backend.repository.TeamRepository;
import ge.mysky.backend.web.NotFoundException;
import jakarta.persistence.criteria.Predicate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OrderService {

    private final OrderRepository orders;
    private final MaterialRepository materials;
    private final FixtureRepository fixtures;
    private final AddonInstanceRepository addons;
    private final TeamRepository teams;
    private final GraniteService granite;
    private final OrderCalculationService calc;
    private final WorkScheduleService workSchedule;

    public OrderService(
            OrderRepository orders,
            MaterialRepository materials,
            FixtureRepository fixtures,
            AddonInstanceRepository addons,
            TeamRepository teams,
            GraniteService granite,
            OrderCalculationService calc,
            WorkScheduleService workSchedule) {
        this.orders = orders;
        this.materials = materials;
        this.fixtures = fixtures;
        this.addons = addons;
        this.teams = teams;
        this.granite = granite;
        this.calc = calc;
        this.workSchedule = workSchedule;
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> list(OffsetDateTime from, OffsetDateTime to, Long teamId, OrderStatus status) {
        Specification<Order> spec = (root, query, cb) -> {
            var preds = new ArrayList<Predicate>();
            if (from != null) preds.add(cb.greaterThanOrEqualTo(root.get("startAt"), from));
            if (to != null) preds.add(cb.lessThan(root.get("startAt"), to));
            if (teamId != null) preds.add(cb.equal(root.get("teamId"), teamId));
            if (status != null) preds.add(cb.equal(root.get("status"), status));
            return cb.and(preds.toArray(new Predicate[0]));
        };
        return orders.findAll(spec, Sort.by(Sort.Direction.ASC, "startAt")).stream()
                .map(OrderResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse get(Long id) {
        return OrderResponse.from(load(id));
    }

    @Transactional
    public OrderResponse create(OrderRequest req) {
        var order = new Order();
        order.setStatus(req.status() == null ? OrderStatus.QUOTED : req.status());
        applyRequest(order, req);
        return OrderResponse.from(orders.save(order));
    }

    @Transactional
    public OrderResponse update(Long id, OrderRequest req) {
        var order = load(id);
        if (req.status() != null) order.setStatus(req.status());
        order.getFixtures().clear();
        order.getAddons().clear();
        applyRequest(order, req);
        return OrderResponse.from(orders.save(order));
    }

    @Transactional
    public void cancel(Long id) {
        var order = load(id);
        order.setStatus(OrderStatus.CANCELLED);
        orders.save(order);
    }

    private Order load(Long id) {
        // Lazy-load the line collections inside the transaction; a single join-fetch of
        // both lists would trigger Hibernate's MultipleBagFetchException.
        return orders.findById(id)
                .orElseThrow(() -> new NotFoundException("Order " + id + " not found"));
    }

    private void applyRequest(Order order, OrderRequest req) {
        order.setClientName(req.clientName().trim());
        order.setClientPhone(blankToNull(req.clientPhone()));
        order.setAddress(blankToNull(req.address()));
        order.setStartAt(req.startAt());
        order.setNotes(blankToNull(req.notes()));
        order.setSquareMeters(req.squareMeters());

        var material = materials.findById(req.materialId())
                .orElseThrow(() -> new NotFoundException("Material " + req.materialId() + " not found"));
        order.setMaterialId(material.getId());
        order.setMaterialName(material.getName());
        order.setMaterialPricePerM2(material.getPricePerM2());
        order.setMaterialTimePerM2Minutes(material.getTimePerM2Minutes());

        ge.mysky.backend.domain.Team team = null;
        if (req.teamId() != null) {
            team = teams.findById(req.teamId())
                    .orElseThrow(() -> new NotFoundException("Team " + req.teamId() + " not found"));
            order.setTeamId(team.getId());
            order.setTeamName(team.getName());
        } else {
            order.setTeamId(null);
            order.setTeamName(null);
        }
        var schedule = workSchedule.resolve(team);
        order.setFlatAddedMinutes(toMinutes(req.flatAddedValue(), req.flatAddedUnit(), schedule));

        order.setGraniteEnabled(req.graniteEnabled());
        if (req.graniteEnabled()) {
            if (req.perimeter() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "perimeter is required when granite is enabled");
            }
            var g = granite.get();
            order.setPerimeter(req.perimeter());
            order.setGranitePricePerMeter(g.getPricePerMeter());
            order.setGraniteTimePerMeterMinutes(g.getTimePerMeterMinutes());
        } else {
            order.setPerimeter(null);
            order.setGranitePricePerMeter(null);
            order.setGraniteTimePerMeterMinutes(null);
        }

        if (req.fixtures() != null) {
            for (var line : req.fixtures()) {
                var fx = fixtures.findById(line.fixtureId())
                        .orElseThrow(() -> new NotFoundException("Fixture " + line.fixtureId() + " not found"));
                var of = new OrderFixture();
                of.setFixtureId(fx.getId());
                of.setName(fx.getName());
                of.setUnit(fx.getUnit());
                of.setUnitCost(fx.getCost());
                of.setUnitTimeMinutes(fx.getInstallTimeMinutes());
                of.setQuantity(line.quantity());
                order.addFixture(of);
            }
        }

        if (req.addons() != null) {
            for (var line : req.addons()) {
                var ad = addons.findById(line.addonId())
                        .orElseThrow(() -> new NotFoundException("Add-on " + line.addonId() + " not found"));
                var oa = new OrderAddon();
                oa.setAddonId(ad.getId());
                oa.setName(ad.getName());
                oa.setCategory(ad.getCategory());
                oa.setUnitCost(ad.getCost());
                oa.setUnitTimeMinutes(ad.getInstallTimeMinutes());
                oa.setQuantity(line.quantity());
                order.addAddon(oa);
            }
        }

        var totals = calc.compute(order);
        order.setTotalMinutes(totals.minutes());
        order.setTotalCost(totals.cost());

        if (req.finishOverridden() && req.finishAt() != null) {
            order.setFinishOverridden(true);
            order.setFinishAt(req.finishAt());
        } else {
            order.setFinishOverridden(false);
            order.setFinishAt(workSchedule.computeFinish(order.getStartAt(), totals.minutes(), schedule));
        }
    }

    private static int toMinutes(java.math.BigDecimal value, ge.mysky.backend.dto.TimeUnit unit,
                                 WorkScheduleService.Resolved schedule) {
        if (value == null) return 0;
        long perUnit = switch (unit == null ? ge.mysky.backend.dto.TimeUnit.MINUTES : unit) {
            case MINUTES -> 1L;
            case HOURS -> 60L;
            case DAYS -> schedule.workdayMinutes();
        };
        return value.multiply(java.math.BigDecimal.valueOf(perUnit))
                .setScale(0, java.math.RoundingMode.HALF_UP).intValueExact();
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
