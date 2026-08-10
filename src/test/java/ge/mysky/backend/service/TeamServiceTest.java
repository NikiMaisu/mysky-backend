package ge.mysky.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import ge.mysky.backend.domain.Role;
import ge.mysky.backend.domain.Team;
import ge.mysky.backend.domain.User;
import ge.mysky.backend.dto.TeamRequest;
import ge.mysky.backend.dto.WorkScheduleDto;
import ge.mysky.backend.repository.TeamRepository;
import ge.mysky.backend.repository.UserRepository;
import ge.mysky.backend.web.NotFoundException;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository teams;

    @Mock
    private UserRepository users;

    private TeamService service;

    @BeforeEach
    void setUp() {
        service = new TeamService(teams, users);
    }

    @Test
    void listReturnsActiveTeams() {
        var team = Team.builder().id(1L).name("Team A").active(true).build();
        when(teams.findAllByActiveTrueOrderByNameAsc()).thenReturn(List.of(team));

        assertThat(service.list()).containsExactly(team);
    }

    @Test
    void getThrowsWhenMissing() {
        when(teams.findWithMembersById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(9L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void createResolvesMembersAndTrimsName() {
        var worker = User.builder().id(5L).name("Ana").role(Role.WORKER).active(true).build();
        when(users.findByIdAndRole(5L, Role.WORKER)).thenReturn(Optional.of(worker));
        when(teams.save(any(Team.class))).thenAnswer(inv -> inv.getArgument(0));

        var req = new TeamRequest("  Team A  ", List.of(5L), null);
        var created = service.create(req);

        assertThat(created.getName()).isEqualTo("Team A");
        assertThat(created.getMembers()).containsExactly(worker);
        assertThat(created.getWorkDays()).isNull();
    }

    @Test
    void createThrowsWhenMemberIsNotAWorker() {
        when(users.findByIdAndRole(5L, Role.WORKER)).thenReturn(Optional.empty());

        var req = new TeamRequest("Team A", List.of(5L), null);

        assertThatThrownBy(() -> service.create(req)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void createAppliesValidSchedule() {
        when(teams.save(any(Team.class))).thenAnswer(inv -> inv.getArgument(0));

        var schedule = new WorkScheduleDto(new boolean[]{true, true, true, true, true, false, false}, "09:00", "17:00");
        var req = new TeamRequest("Team A", null, schedule);
        var created = service.create(req);

        assertThat(created.getWorkStart()).isEqualTo(LocalTime.of(9, 0));
        assertThat(created.getWorkEnd()).isEqualTo(LocalTime.of(17, 0));
        assertThat(created.getWorkDays()).isNotNull();
    }

    @Test
    void createRejectsScheduleWithEndBeforeStart() {
        var schedule = new WorkScheduleDto(new boolean[]{true, false, false, false, false, false, false}, "17:00", "09:00");
        var req = new TeamRequest("Team A", null, schedule);

        assertThatThrownBy(() -> service.create(req)).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void updateOnlyReplacesMembersWhenProvided() {
        var existingMember = User.builder().id(1L).name("Old").role(Role.WORKER).active(true).build();
        var team = Team.builder().id(1L).name("Old name").active(true)
                .members(new java.util.LinkedHashSet<>(List.of(existingMember))).build();
        when(teams.findWithMembersById(1L)).thenReturn(Optional.of(team));
        when(teams.save(any(Team.class))).thenAnswer(inv -> inv.getArgument(0));

        var req = new TeamRequest("New name", null, null);
        var updated = service.update(1L, req);

        assertThat(updated.getName()).isEqualTo("New name");
        assertThat(updated.getMembers()).containsExactly(existingMember);
    }

    @Test
    void updateClearsScheduleWhenNull() {
        var team = Team.builder().id(1L).name("Team A").active(true)
                .workDays((short) 31).workStart(LocalTime.of(9, 0)).workEnd(LocalTime.of(17, 0)).build();
        when(teams.findWithMembersById(1L)).thenReturn(Optional.of(team));
        when(teams.save(any(Team.class))).thenAnswer(inv -> inv.getArgument(0));

        var updated = service.update(1L, new TeamRequest("Team A", null, null));

        assertThat(updated.getWorkDays()).isNull();
        assertThat(updated.getWorkStart()).isNull();
        assertThat(updated.getWorkEnd()).isNull();
    }

    @Test
    void deleteMarksInactiveInsteadOfRemoving() {
        var team = Team.builder().id(1L).name("Team A").active(true).build();
        when(teams.findWithMembersById(1L)).thenReturn(Optional.of(team));
        when(teams.save(any(Team.class))).thenAnswer(inv -> inv.getArgument(0));

        service.delete(1L);

        assertThat(team.isActive()).isFalse();
    }

    @Test
    void addMemberAppendsWorkerToTeam() {
        var team = Team.builder().id(1L).name("Team A").active(true).build();
        var worker = User.builder().id(5L).name("Ana").role(Role.WORKER).active(true).build();
        when(teams.findWithMembersById(1L)).thenReturn(Optional.of(team));
        when(users.findByIdAndRole(5L, Role.WORKER)).thenReturn(Optional.of(worker));
        when(teams.save(any(Team.class))).thenAnswer(inv -> inv.getArgument(0));

        var updated = service.addMember(1L, 5L);

        assertThat(updated.getMembers()).containsExactly(worker);
    }

    @Test
    void removeMemberDropsWorkerFromTeam() {
        var worker = User.builder().id(5L).name("Ana").role(Role.WORKER).active(true).build();
        var team = Team.builder().id(1L).name("Team A").active(true)
                .members(new java.util.LinkedHashSet<>(List.of(worker))).build();
        when(teams.findWithMembersById(1L)).thenReturn(Optional.of(team));
        when(teams.save(any(Team.class))).thenAnswer(inv -> inv.getArgument(0));

        var updated = service.removeMember(1L, 5L);

        assertThat(updated.getMembers()).isEmpty();
    }
}
