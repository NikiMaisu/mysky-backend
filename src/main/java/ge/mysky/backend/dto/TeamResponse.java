package ge.mysky.backend.dto;

import ge.mysky.backend.domain.Team;
import ge.mysky.backend.service.WorkScheduleService;
import java.util.List;

public record TeamResponse(
        Long id,
        String name,
        boolean active,
        WorkScheduleDto schedule,
        List<WorkerResponse> members) {

    public static TeamResponse from(Team t) {
        var members = t.getMembers().stream()
                .map(WorkerResponse::from)
                .sorted((a, b) -> a.name().compareToIgnoreCase(b.name()))
                .toList();
        WorkScheduleDto schedule = null;
        if (t.getWorkDays() != null && t.getWorkStart() != null && t.getWorkEnd() != null) {
            schedule = new WorkScheduleDto(
                    WorkScheduleService.toDays(t.getWorkDays()),
                    t.getWorkStart().toString(),
                    t.getWorkEnd().toString());
        }
        return new TeamResponse(t.getId(), t.getName(), t.isActive(), schedule, members);
    }
}
