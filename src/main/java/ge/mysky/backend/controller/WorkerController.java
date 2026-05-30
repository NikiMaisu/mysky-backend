package ge.mysky.backend.controller;

import ge.mysky.backend.dto.WorkerCreateRequest;
import ge.mysky.backend.dto.WorkerResponse;
import ge.mysky.backend.dto.WorkerUpdateRequest;
import ge.mysky.backend.service.WorkerService;
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
@RequestMapping("/workers")
@PreAuthorize("hasRole('ADMIN')")
public class WorkerController {

    private final WorkerService service;

    public WorkerController(WorkerService service) {
        this.service = service;
    }

    @GetMapping
    public List<WorkerResponse> list() {
        return service.list().stream().map(WorkerResponse::from).toList();
    }

    @GetMapping("/{id}")
    public WorkerResponse get(@PathVariable Long id) {
        return WorkerResponse.from(service.get(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkerResponse create(@Valid @RequestBody WorkerCreateRequest req) {
        return WorkerResponse.from(service.create(req));
    }

    @PutMapping("/{id}")
    public WorkerResponse update(@PathVariable Long id, @Valid @RequestBody WorkerUpdateRequest req) {
        return WorkerResponse.from(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
