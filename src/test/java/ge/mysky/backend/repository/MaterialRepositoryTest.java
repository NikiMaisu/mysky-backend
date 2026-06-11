package ge.mysky.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import ge.mysky.backend.AbstractIntegrationTest;
import ge.mysky.backend.domain.Material;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class MaterialRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    MaterialRepository repo;

    private Material material(String name, boolean active) {
        return Material.builder()
                .name(name)
                .pricePerM2(new BigDecimal("10.00"))
                .timePerM2Minutes(new BigDecimal("5.00"))
                .active(active)
                .build();
    }

    @Test
    void activeOnlyAndOrderedByName() {
        repo.save(material("ZZ-Material", true));
        repo.save(material("AA-Material", true));
        repo.save(material("Inactive-Material", false));

        var active = repo.findAllByActiveTrueOrderByNameAsc();
        var names = active.stream().map(Material::getName).toList();

        assertThat(names).contains("AA-Material", "ZZ-Material");
        assertThat(names).doesNotContain("Inactive-Material");
        assertThat(names.indexOf("AA-Material")).isLessThan(names.indexOf("ZZ-Material"));
    }

    @Test
    void timestampsSetOnPersist() {
        var saved = repo.save(material("Timestamped", true));
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }
}
