package ge.mysky.backend.controller;

import ge.mysky.backend.dto.FixtureRequest;
import ge.mysky.backend.dto.FixtureResponse;
import ge.mysky.backend.service.FixtureService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fixtures")
public class FixtureController {

    private final FixtureService service;

    public FixtureController(FixtureService service) {
        this.service = service;
    }

    @GetMapping
    public List<FixtureResponse> list(
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        return service.list(includeInactive).stream().map(FixtureResponse::from).toList();
    }

    @GetMapping("/{id}")
    public FixtureResponse get(@PathVariable Long id) {
        return FixtureResponse.from(service.get(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public FixtureResponse create(@Valid @RequestBody FixtureRequest req) {
        return FixtureResponse.from(service.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public FixtureResponse update(@PathVariable Long id, @Valid @RequestBody FixtureRequest req) {
        return FixtureResponse.from(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
