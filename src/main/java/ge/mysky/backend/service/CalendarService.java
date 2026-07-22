package ge.mysky.backend.service;

import ge.mysky.backend.domain.Order;
import ge.mysky.backend.domain.OrderStatus;
import ge.mysky.backend.dto.CalendarOrder;
import ge.mysky.backend.dto.DayAvailability;
import ge.mysky.backend.dto.TeamRef;
import ge.mysky.backend.repository.OrderRepository;
import ge.mysky.backend.repository.TeamRepository;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CalendarService {

    // mysky.ge operates in Tbilisi (fixed UTC+4, no DST).
    private static final ZoneId ZONE = ZoneId.of("Asia/Tbilisi");
    private static final long MAX_DAYS = 92;

    private final OrderRepository orders;
    private final TeamRepository teams;

    public CalendarService(OrderRepository orders, TeamRepository teams) {
        this.orders = orders;
        this.teams = teams;
    }

    @Transactional(readOnly = true)
    public List<CalendarOrder> ordersInRange(OffsetDateTime from, OffsetDateTime to, ge.mysky.backend.domain.User currentUser) {
        List<Long> ownTeamIds = null;
        if (currentUser.getRole() == ge.mysky.backend.domain.Role.WORKER) {
            ownTeamIds = teams.findByMembers_Id(currentUser.getId()).stream()
                    .map(ge.mysky.backend.domain.Team::getId).toList();
            if (ownTeamIds.isEmpty()) return List.of();
        }
        return orders.findAll(overlap(from, to, ownTeamIds), Sort.by(Sort.Direction.ASC, "startAt"))
                .stream().map(CalendarOrder::from).toList();
    }

    @Transactional(readOnly = true)
    public List<DayAvailability> availability(OffsetDateTime from, OffsetDateTime to) {
        LocalDate first = from.atZoneSameInstant(ZONE).toLocalDate();
        LocalDate last = to.minusNanos(1).atZoneSameInstant(ZONE).toLocalDate();
        if (last.isBefore(first)) last = first;
        if (ChronoUnit.DAYS.between(first, last) + 1 > MAX_DAYS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Range too large (max " + MAX_DAYS + " days)");
        }

        var allTeams = teams.findAllByActiveTrueOrderByNameAsc();
        var assigned = orders.findAll(overlap(from, to, null)).stream()
                .filter(o -> o.getTeamId() != null)
                .toList();

        var result = new ArrayList<DayAvailability>();
        for (LocalDate d = first; !d.isAfter(last); d = d.plusDays(1)) {
            var dayStart = d.atStartOfDay(ZONE).toOffsetDateTime();
            var dayEnd = d.plusDays(1).atStartOfDay(ZONE).toOffsetDateTime();
            var busyIds = new HashSet<Long>();
            for (Order o : assigned) {
                if (o.getStartAt().isBefore(dayEnd) && o.getFinishAt().isAfter(dayStart)) {
                    busyIds.add(o.getTeamId());
                }
            }
            var free = new ArrayList<TeamRef>();
            var busy = new ArrayList<TeamRef>();
            for (var t : allTeams) {
                (busyIds.contains(t.getId()) ? busy : free).add(TeamRef.from(t));
            }
            result.add(new DayAvailability(d, free, busy));
        }
        return result;
    }

    private Specification<Order> overlap(OffsetDateTime from, OffsetDateTime to, List<Long> restrictTeamIds) {
        return (root, query, cb) -> {
            var preds = new ArrayList<Predicate>();
            preds.add(cb.lessThan(root.get("startAt"), to));
            preds.add(cb.greaterThan(root.get("finishAt"), from));
            preds.add(cb.notEqual(root.get("status"), OrderStatus.CANCELLED));
            if (restrictTeamIds != null) preds.add(root.get("teamId").in(restrictTeamIds));
            return cb.and(preds.toArray(new Predicate[0]));
        };
    }
}
