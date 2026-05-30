package ge.mysky.backend.controller;

import ge.mysky.backend.dto.TeamMemberRequest;
import ge.mysky.backend.dto.TeamRequest;
import ge.mysky.backend.dto.TeamResponse;
import ge.mysky.backend.service.TeamService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/teams")
public class TeamController {

    private final TeamService service;

    public TeamController(TeamService service) {
        this.service = service;
    }

    @GetMapping
    public List<TeamResponse> list() {
        return service.list().stream().map(TeamResponse::from).toList();
    }

    @GetMapping("/{id}")
    public TeamResponse get(@PathVariable Long id) {
        return TeamResponse.from(service.get(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public TeamResponse create(@Valid @RequestBody TeamRequest req) {
        return TeamResponse.from(service.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public TeamResponse update(@PathVariable Long id, @Valid @RequestBody TeamRequest req) {
        return TeamResponse.from(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("hasRole('ADMIN')")
    public TeamResponse addMember(@PathVariable Long id, @Valid @RequestBody TeamMemberRequest req) {
        return TeamResponse.from(service.addMember(id, req.workerId()));
    }

    @DeleteMapping("/{id}/members/{workerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public TeamResponse removeMember(@PathVariable Long id, @PathVariable Long workerId) {
        return TeamResponse.from(service.removeMember(id, workerId));
    }
}
