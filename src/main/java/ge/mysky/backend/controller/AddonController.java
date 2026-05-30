package ge.mysky.backend.controller;

import ge.mysky.backend.domain.AddonCategory;
import ge.mysky.backend.dto.AddonRequest;
import ge.mysky.backend.dto.AddonResponse;
import ge.mysky.backend.service.AddonService;
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
@RequestMapping("/addons")
public class AddonController {

    private final AddonService service;

    public AddonController(AddonService service) {
        this.service = service;
    }

    @GetMapping
    public List<AddonResponse> list(
            @RequestParam(required = false) AddonCategory category,
            @RequestParam(defaultValue = "false") boolean includeInactive) {
        return service.list(category, includeInactive).stream().map(AddonResponse::from).toList();
    }

    @GetMapping("/{id}")
    public AddonResponse get(@PathVariable Long id) {
        return AddonResponse.from(service.get(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public AddonResponse create(@Valid @RequestBody AddonRequest req) {
        return AddonResponse.from(service.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AddonResponse update(@PathVariable Long id, @Valid @RequestBody AddonRequest req) {
        return AddonResponse.from(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
