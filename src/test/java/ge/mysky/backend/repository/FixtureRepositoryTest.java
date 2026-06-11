package ge.mysky.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import ge.mysky.backend.AbstractIntegrationTest;
import ge.mysky.backend.domain.Fixture;
import ge.mysky.backend.domain.FixtureUnit;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class FixtureRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    FixtureRepository repo;

    @Test
    void persistsUnitAndFiltersActive() {
        var saved = repo.save(Fixture.builder()
                .name("LED strip test")
                .unit(FixtureUnit.PER_METER)
                .cost(new BigDecimal("8.00"))
                .installTimeMinutes(new BigDecimal("5.00"))
                .active(true)
                .build());
        repo.save(Fixture.builder()
                .name("Retired fixture")
                .unit(FixtureUnit.PER_UNIT)
                .cost(new BigDecimal("1.00"))
                .installTimeMinutes(new BigDecimal("1.00"))
                .active(false)
                .build());

        var found = repo.findById(saved.getId()).orElseThrow();
        assertThat(found.getUnit()).isEqualTo(FixtureUnit.PER_METER);

        var names = repo.findAllByActiveTrueOrderByNameAsc().stream().map(Fixture::getName).toList();
        assertThat(names).contains("LED strip test").doesNotContain("Retired fixture");
    }
}
