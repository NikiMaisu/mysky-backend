package ge.mysky.backend.controller;

import ge.mysky.backend.dto.GraniteConfigRequest;
import ge.mysky.backend.dto.GraniteConfigResponse;
import ge.mysky.backend.service.GraniteService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/granite")
public class GraniteController {

    private final GraniteService service;

    public GraniteController(GraniteService service) {
        this.service = service;
    }

    @GetMapping
    public GraniteConfigResponse get() {
        return GraniteConfigResponse.from(service.get());
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public GraniteConfigResponse update(@Valid @RequestBody GraniteConfigRequest req) {
        return GraniteConfigResponse.from(service.update(req));
    }
}
