package ge.mysky.backend.dto;

import ge.mysky.backend.domain.Team;
import java.util.List;

public record TeamResponse(
        Long id,
        String name,
        boolean active,
        List<WorkerResponse> members) {

    public static TeamResponse from(Team t) {
        var members = t.getMembers().stream()
                .map(WorkerResponse::from)
                .sorted((a, b) -> a.name().compareToIgnoreCase(b.name()))
                .toList();
        return new TeamResponse(t.getId(), t.getName(), t.isActive(), members);
    }
}
