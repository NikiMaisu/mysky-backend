package ge.mysky.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ge.mysky.backend.domain.Material;
import ge.mysky.backend.dto.MaterialRequest;
import ge.mysky.backend.repository.MaterialRepository;
import ge.mysky.backend.web.NotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MaterialServiceTest {

    @Mock
    private MaterialRepository materials;

    private MaterialService service;

    @BeforeEach
    void setUp() {
        service = new MaterialService(materials);
    }

    @Test
    void listReturnsActiveOnlyByDefault() {
        var material = Material.builder().id(1L).name("Matte white").active(true).build();
        when(materials.findAllByActiveTrueOrderByNameAsc()).thenReturn(List.of(material));

        assertThat(service.list(false)).containsExactly(material);
    }

    @Test
    void listIncludesInactiveWhenRequested() {
        var active = Material.builder().id(1L).name("Matte white").active(true).build();
        var inactive = Material.builder().id(2L).name("Old material").active(false).build();
        when(materials.findAll()).thenReturn(List.of(active, inactive));

        assertThat(service.list(true)).containsExactly(active, inactive);
    }

    @Test
    void getThrowsWhenMissing() {
        when(materials.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(9L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void createTrimsNameAndSetsActive() {
        var req = new MaterialRequest("  Matte white  ", BigDecimal.valueOf(25), BigDecimal.valueOf(12));
        when(materials.save(any(Material.class))).thenAnswer(inv -> inv.getArgument(0));

        var created = service.create(req);

        assertThat(created.getName()).isEqualTo("Matte white");
        assertThat(created.isActive()).isTrue();
    }

    @Test
    void updateOverwritesFields() {
        var existing = Material.builder().id(1L).name("Old").pricePerM2(BigDecimal.ONE)
                .timePerM2Minutes(BigDecimal.ONE).active(true).build();
        when(materials.findById(1L)).thenReturn(Optional.of(existing));
        when(materials.save(any(Material.class))).thenAnswer(inv -> inv.getArgument(0));

        var req = new MaterialRequest("New name", BigDecimal.valueOf(40), BigDecimal.valueOf(15));
        var updated = service.update(1L, req);

        assertThat(updated.getName()).isEqualTo("New name");
        assertThat(updated.getPricePerM2()).isEqualByComparingTo(BigDecimal.valueOf(40));
        assertThat(updated.getTimePerM2Minutes()).isEqualByComparingTo(BigDecimal.valueOf(15));
    }

    @Test
    void deleteMarksInactiveInsteadOfRemoving() {
        var existing = Material.builder().id(1L).name("Matte white").active(true).build();
        when(materials.findById(1L)).thenReturn(Optional.of(existing));
        when(materials.save(any(Material.class))).thenAnswer(inv -> inv.getArgument(0));

        service.delete(1L);

        assertThat(existing.isActive()).isFalse();
        verify(materials).save(existing);
    }
}
