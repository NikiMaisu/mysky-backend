package ge.mysky.backend.controller;

import ge.mysky.backend.dto.MaterialRequest;
import ge.mysky.backend.dto.MaterialResponse;
import ge.mysky.backend.service.MaterialService;
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
@RequestMapping("/materials")
public class MaterialController {

    private final MaterialService service;

    public MaterialController(MaterialService service) {
        this.service = service;
    }

    @GetMapping
    public List<MaterialResponse> list(
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        return service.list(includeInactive).stream().map(MaterialResponse::from).toList();
    }

    @GetMapping("/{id}")
    public MaterialResponse get(@PathVariable Long id) {
        return MaterialResponse.from(service.get(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public MaterialResponse create(@Valid @RequestBody MaterialRequest req) {
        return MaterialResponse.from(service.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public MaterialResponse update(@PathVariable Long id, @Valid @RequestBody MaterialRequest req) {
        return MaterialResponse.from(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
