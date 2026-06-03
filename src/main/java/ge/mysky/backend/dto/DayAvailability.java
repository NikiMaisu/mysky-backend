package ge.mysky.backend.dto;

import java.time.LocalDate;
import java.util.List;

public record DayAvailability(
        LocalDate date,
        List<TeamRef> freeTeams,
        List<TeamRef> busyTeams) {
}
