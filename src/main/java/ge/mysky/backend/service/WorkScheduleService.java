package ge.mysky.backend.service;

import ge.mysky.backend.domain.Team;
import ge.mysky.backend.domain.WorkSchedule;
import ge.mysky.backend.repository.WorkScheduleRepository;
import ge.mysky.backend.web.NotFoundException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class WorkScheduleService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Tbilisi");
    private static final int MAX_DAYS_SCANNED = 4000;

    private final WorkScheduleRepository repo;

    public WorkScheduleService(WorkScheduleRepository repo) {
        this.repo = repo;
    }

    /** Effective schedule: day-of-week bitmask (Mon=1..Sun=64) + daily window. */
    public record Resolved(short workDays, LocalTime start, LocalTime end) {

        public boolean isWorkingDay(LocalDate d) {
            return (workDays & (1 << (d.getDayOfWeek().getValue() - 1))) != 0;
        }

        public long workdayMinutes() {
            return Duration.between(start, end).toMinutes();
        }
    }

    @Transactional(readOnly = true)
    public WorkSchedule getGlobal() {
        return repo.findById(WorkSchedule.SINGLETON_ID)
                .orElseThrow(() -> new NotFoundException("Work schedule not initialized"));
    }

    @Transactional
    public WorkSchedule updateGlobal(boolean[] days, LocalTime start, LocalTime end) {
        var schedule = getGlobal();
        schedule.setWorkDays(validateAndPack(days, start, end));
        schedule.setStartTime(start);
        schedule.setEndTime(end);
        return repo.save(schedule);
    }

    /** Validates a schedule and returns the day bitmask. */
    public static short validateAndPack(boolean[] days, LocalTime start, LocalTime end) {
        if (days == null || days.length != 7) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "days must have 7 entries (Mon–Sun)");
        }
        if (start == null || end == null || !end.isAfter(start)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "end time must be after start time");
        }
        short mask = toBitmask(days);
        if (mask == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "at least one working day is required");
        }
        return mask;
    }

    public static short toBitmask(boolean[] days) {
        short mask = 0;
        for (int i = 0; i < 7; i++) {
            if (days[i]) mask |= (short) (1 << i);
        }
        return mask;
    }

    public static boolean[] toDays(short mask) {
        var days = new boolean[7];
        for (int i = 0; i < 7; i++) days[i] = (mask & (1 << i)) != 0;
        return days;
    }

    public Resolved resolveGlobal() {
        var g = getGlobal();
        return new Resolved(g.getWorkDays(), g.getStartTime(), g.getEndTime());
    }

    /** Team override when fully set, otherwise the global schedule. */
    public Resolved resolve(Team team) {
        if (team != null && team.getWorkDays() != null && team.getWorkStart() != null && team.getWorkEnd() != null) {
            return new Resolved(team.getWorkDays(), team.getWorkStart(), team.getWorkEnd());
        }
        return resolveGlobal();
    }

    /**
     * Finish instant after laying {@code minutes} of work across the schedule's working
     * windows starting from {@code startAt}, skipping nights and non-working days.
     */
    public OffsetDateTime computeFinish(OffsetDateTime startAt, long minutes, Resolved s) {
        if (minutes <= 0 || s.workDays() == 0) {
            return startAt;
        }
        ZonedDateTime cursor = startAt.atZoneSameInstant(ZONE);
        long remaining = minutes;

        for (int guard = 0; guard < MAX_DAYS_SCANNED; guard++) {
            LocalDate date = cursor.toLocalDate();
            if (!s.isWorkingDay(date)) {
                cursor = date.plusDays(1).atStartOfDay(ZONE);
                continue;
            }
            ZonedDateTime winStart = date.atTime(s.start()).atZone(ZONE);
            ZonedDateTime winEnd = date.atTime(s.end()).atZone(ZONE);
            if (cursor.isBefore(winStart)) {
                cursor = winStart;
            }
            if (!cursor.isBefore(winEnd)) {
                cursor = date.plusDays(1).atStartOfDay(ZONE);
                continue;
            }
            long avail = Duration.between(cursor, winEnd).toMinutes();
            if (remaining <= avail) {
                return cursor.plusMinutes(remaining).toOffsetDateTime();
            }
            remaining -= avail;
            cursor = date.plusDays(1).atStartOfDay(ZONE);
        }
        return cursor.toOffsetDateTime();
    }
}
