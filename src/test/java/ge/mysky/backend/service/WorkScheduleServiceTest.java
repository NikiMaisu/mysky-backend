package ge.mysky.backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import ge.mysky.backend.domain.Team;
import ge.mysky.backend.service.WorkScheduleService.Resolved;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class WorkScheduleServiceTest {

    // repo is only used by getGlobal/resolveGlobal; computeFinish + resolve(override) don't touch it.
    private final WorkScheduleService svc = new WorkScheduleService(null);

    // Mon–Sat (1+2+4+8+16+32 = 63), 10:00–18:00.
    private static final Resolved MON_SAT = new Resolved((short) 63, LocalTime.of(10, 0), LocalTime.of(18, 0));

    private static OffsetDateTime at(int y, int mo, int d, int h, int mi) {
        return OffsetDateTime.of(y, mo, d, h, mi, 0, 0, ZoneOffset.ofHours(4));
    }

    @Test
    void workdayMinutes() {
        assertThat(MON_SAT.workdayMinutes()).isEqualTo(480);
    }

    @Test
    void zeroDurationReturnsStart() {
        var start = at(2026, 6, 8, 10, 0);
        assertThat(svc.computeFinish(start, 0, MON_SAT)).isEqualTo(start);
    }

    @Test
    void overflowAcrossWorkingDays() {
        // 23h job starting Mon 10:00 -> Mon 8h + Tue 8h + Wed 7h => Wed 17:00.
        var finish = svc.computeFinish(at(2026, 6, 8, 10, 0), 23 * 60, MON_SAT);
        assertThat(finish.isEqual(at(2026, 6, 10, 17, 0))).isTrue();
    }

    @Test
    void twoFullWorkdays() {
        var finish = svc.computeFinish(at(2026, 6, 8, 10, 0), 960, MON_SAT); // 2 * 8h
        assertThat(finish.isEqual(at(2026, 6, 9, 18, 0))).isTrue();
    }

    @Test
    void partialDayThenRolls() {
        // Start Mon 14:00, 8h: Mon 14-18 (4h) then Tue 10-14 (4h) => Tue 14:00.
        var finish = svc.computeFinish(at(2026, 6, 8, 14, 0), 480, MON_SAT);
        assertThat(finish.isEqual(at(2026, 6, 9, 14, 0))).isTrue();
    }

    @Test
    void startOnNonWorkingDayMovesToNextWorkingWindow() {
        // 2026-06-07 is a Sunday (non-working) -> first work happens Mon 10:00.
        var finish = svc.computeFinish(at(2026, 6, 7, 12, 0), 60, MON_SAT);
        assertThat(finish.isEqual(at(2026, 6, 8, 11, 0))).isTrue();
    }

    @Test
    void resolveUsesTeamOverrideWhenPresent() {
        var team = Team.builder()
                .workDays((short) 127) // all days
                .workStart(LocalTime.of(8, 0))
                .workEnd(LocalTime.of(20, 0))
                .build();
        var r = svc.resolve(team);
        assertThat(r.workDays()).isEqualTo((short) 127);
        assertThat(r.start()).isEqualTo(LocalTime.of(8, 0));
        assertThat(r.workdayMinutes()).isEqualTo(720);
    }
}
