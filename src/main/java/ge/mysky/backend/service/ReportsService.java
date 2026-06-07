package ge.mysky.backend.service;

import ge.mysky.backend.domain.Order;
import ge.mysky.backend.domain.OrderStatus;
import ge.mysky.backend.dto.BrigadeReport;
import ge.mysky.backend.dto.WorkerReport;
import ge.mysky.backend.repository.OrderRepository;
import ge.mysky.backend.repository.TeamRepository;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportsService {

    private final OrderRepository orders;
    private final TeamRepository teams;

    public ReportsService(OrderRepository orders, TeamRepository teams) {
        this.orders = orders;
        this.teams = teams;
    }

    private static final class Agg {
        String name;
        long count;
        long minutes;
        BigDecimal sqm = BigDecimal.ZERO;
        BigDecimal cost = BigDecimal.ZERO;

        void add(Order o) {
            count++;
            minutes += o.getTotalMinutes();
            sqm = sqm.add(o.getSquareMeters());
            cost = cost.add(o.getTotalCost());
        }
    }

    @Transactional(readOnly = true)
    public List<BrigadeReport> brigades(OffsetDateTime from, OffsetDateTime to) {
        var byTeam = aggregateByTeam(from, to);
        var result = new ArrayList<BrigadeReport>();
        byTeam.forEach((teamId, a) ->
                result.add(new BrigadeReport(teamId, a.name, a.count, a.minutes, a.sqm, a.cost)));
        result.sort(Comparator.comparing(r -> r.teamName() == null ? "" : r.teamName().toLowerCase()));
        return result;
    }

    @Transactional(readOnly = true)
    public List<WorkerReport> workers(OffsetDateTime from, OffsetDateTime to) {
        var byTeam = aggregateByTeam(from, to);
        // Attribute each team's work to its current members (full hours/m² to each).
        var byWorker = new LinkedHashMap<Long, Agg>();
        for (var team : teams.findAll()) {
            var ta = byTeam.get(team.getId());
            if (ta == null) continue;
            for (var member : team.getMembers()) {
                var wa = byWorker.computeIfAbsent(member.getId(), k -> new Agg());
                wa.name = member.getName();
                wa.count += ta.count;
                wa.minutes += ta.minutes;
                wa.sqm = wa.sqm.add(ta.sqm);
            }
        }
        var result = new ArrayList<WorkerReport>();
        byWorker.forEach((workerId, a) ->
                result.add(new WorkerReport(workerId, a.name, a.count, a.minutes, a.sqm)));
        result.sort(Comparator.comparing(r -> r.name() == null ? "" : r.name().toLowerCase()));
        return result;
    }

    /** Per-team aggregates for non-cancelled orders starting within [from, to). */
    private Map<Long, Agg> aggregateByTeam(OffsetDateTime from, OffsetDateTime to) {
        Specification<Order> spec = (root, query, cb) -> {
            var preds = new ArrayList<Predicate>();
            preds.add(cb.greaterThanOrEqualTo(root.get("startAt"), from));
            preds.add(cb.lessThan(root.get("startAt"), to));
            preds.add(cb.notEqual(root.get("status"), OrderStatus.CANCELLED));
            preds.add(cb.isNotNull(root.get("teamId")));
            return cb.and(preds.toArray(new Predicate[0]));
        };
        var byTeam = new LinkedHashMap<Long, Agg>();
        for (var o : orders.findAll(spec)) {
            var a = byTeam.computeIfAbsent(o.getTeamId(), k -> new Agg());
            a.name = o.getTeamName();
            a.add(o);
        }
        return byTeam;
    }
}
