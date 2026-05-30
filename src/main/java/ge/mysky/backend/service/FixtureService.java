package ge.mysky.backend.service;

import ge.mysky.backend.domain.Fixture;
import ge.mysky.backend.dto.FixtureRequest;
import ge.mysky.backend.repository.FixtureRepository;
import ge.mysky.backend.web.NotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FixtureService {

    private final FixtureRepository fixtures;

    public FixtureService(FixtureRepository fixtures) {
        this.fixtures = fixtures;
    }

    @Transactional(readOnly = true)
    public List<Fixture> list(boolean includeInactive) {
        return includeInactive ? fixtures.findAll() : fixtures.findAllByActiveTrueOrderByNameAsc();
    }

    @Transactional(readOnly = true)
    public Fixture get(Long id) {
        return fixtures.findById(id)
                .orElseThrow(() -> new NotFoundException("Fixture " + id + " not found"));
    }

    @Transactional
    public Fixture create(FixtureRequest req) {
        var fixture = Fixture.builder()
                .name(req.name().trim())
                .unit(req.unit())
                .cost(req.cost())
                .installTimeMinutes(req.installTimeMinutes())
                .active(true)
                .build();
        return fixtures.save(fixture);
    }

    @Transactional
    public Fixture update(Long id, FixtureRequest req) {
        var fixture = get(id);
        fixture.setName(req.name().trim());
        fixture.setUnit(req.unit());
        fixture.setCost(req.cost());
        fixture.setInstallTimeMinutes(req.installTimeMinutes());
        return fixtures.save(fixture);
    }

    @Transactional
    public void delete(Long id) {
        var fixture = get(id);
        fixture.setActive(false);
        fixtures.save(fixture);
    }
}
