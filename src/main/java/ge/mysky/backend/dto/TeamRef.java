package ge.mysky.backend.dto;

import ge.mysky.backend.domain.Team;

public record TeamRef(Long id, String name) {

    public static TeamRef from(Team t) {
        return new TeamRef(t.getId(), t.getName());
    }
}
