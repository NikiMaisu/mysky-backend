package ge.mysky.backend.service;

import ge.mysky.backend.domain.Role;
import ge.mysky.backend.domain.Team;
import ge.mysky.backend.domain.User;
import ge.mysky.backend.dto.TeamRequest;
import ge.mysky.backend.repository.TeamRepository;
import ge.mysky.backend.repository.UserRepository;
import ge.mysky.backend.web.NotFoundException;
import java.util.LinkedHashSet;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeamService {

    private final TeamRepository teams;
    private final UserRepository users;

    public TeamService(TeamRepository teams, UserRepository users) {
        this.teams = teams;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<Team> list() {
        return teams.findAllByActiveTrueOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public Team get(Long id) {
        return teams.findWithMembersById(id)
                .orElseThrow(() -> new NotFoundException("Team " + id + " not found"));
    }

    @Transactional
    public Team create(TeamRequest req) {
        var team = Team.builder()
                .name(req.name().trim())
                .active(true)
                .members(resolveMembers(req.memberIds()))
                .build();
        applySchedule(team, req.schedule());
        return teams.save(team);
    }

    @Transactional
    public Team update(Long id, TeamRequest req) {
        var team = get(id);
        team.setName(req.name().trim());
        if (req.memberIds() != null) {
            team.setMembers(resolveMembers(req.memberIds()));
        }
        applySchedule(team, req.schedule());
        return teams.save(team);
    }

    private void applySchedule(Team team, ge.mysky.backend.dto.WorkScheduleDto schedule) {
        if (schedule == null) {
            team.setWorkDays(null);
            team.setWorkStart(null);
            team.setWorkEnd(null);
            return;
        }
        var start = java.time.LocalTime.parse(schedule.start());
        var end = java.time.LocalTime.parse(schedule.end());
        team.setWorkDays(ge.mysky.backend.service.WorkScheduleService.validateAndPack(schedule.days(), start, end));
        team.setWorkStart(start);
        team.setWorkEnd(end);
    }

    @Transactional
    public void delete(Long id) {
        var team = get(id);
        team.setActive(false);
        teams.save(team);
    }

    @Transactional
    public Team addMember(Long teamId, Long workerId) {
        var team = get(teamId);
        team.getMembers().add(requireWorker(workerId));
        return teams.save(team);
    }

    @Transactional
    public Team removeMember(Long teamId, Long workerId) {
        var team = get(teamId);
        team.getMembers().removeIf(m -> m.getId().equals(workerId));
        return teams.save(team);
    }

    private LinkedHashSet<User> resolveMembers(List<Long> ids) {
        var members = new LinkedHashSet<User>();
        if (ids != null) {
            for (var id : ids) {
                members.add(requireWorker(id));
            }
        }
        return members;
    }

    private User requireWorker(Long workerId) {
        return users.findByIdAndRole(workerId, Role.WORKER)
                .orElseThrow(() -> new NotFoundException("Worker " + workerId + " not found"));
    }
}
