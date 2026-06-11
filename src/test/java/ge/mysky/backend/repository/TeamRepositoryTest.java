package ge.mysky.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import ge.mysky.backend.AbstractIntegrationTest;
import ge.mysky.backend.domain.Role;
import ge.mysky.backend.domain.Team;
import ge.mysky.backend.domain.User;
import java.util.LinkedHashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class TeamRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    TeamRepository teams;

    @Autowired
    UserRepository users;

    private User worker(String email) {
        return users.save(User.builder()
                .name("Worker " + email)
                .email(email)
                .passwordHash("x")
                .role(Role.WORKER)
                .active(true)
                .build());
    }

    @Test
    void savesAndLoadsMembers() {
        var a = worker("brigtest-a@mysky.ge");
        var b = worker("brigtest-b@mysky.ge");
        var team = teams.save(Team.builder()
                .name("Brigade Test")
                .active(true)
                .members(new LinkedHashSet<>(Set.of(a, b)))
                .build());

        var loaded = teams.findWithMembersById(team.getId()).orElseThrow();
        assertThat(loaded.getMembers()).extracting(User::getId).containsExactlyInAnyOrder(a.getId(), b.getId());
    }

    @Test
    void activeFilter() {
        teams.save(Team.builder().name("Active Brigade").active(true).build());
        teams.save(Team.builder().name("Dissolved Brigade").active(false).build());

        var names = teams.findAllByActiveTrueOrderByNameAsc().stream().map(Team::getName).toList();
        assertThat(names).contains("Active Brigade").doesNotContain("Dissolved Brigade");
    }
}
