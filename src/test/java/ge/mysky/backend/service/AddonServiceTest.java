package ge.mysky.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ge.mysky.backend.domain.AddonCategory;
import ge.mysky.backend.domain.AddonInstance;
import ge.mysky.backend.dto.AddonRequest;
import ge.mysky.backend.repository.AddonInstanceRepository;
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
class AddonServiceTest {

    @Mock
    private AddonInstanceRepository addons;

    private AddonService service;

    @BeforeEach
    void setUp() {
        service = new AddonService(addons);
    }

    @Test
    void listReturnsActiveOnlyByDefault() {
        var addon = AddonInstance.builder().id(1L).name("Blinds").active(true).build();
        when(addons.findAllByActiveTrueOrderByNameAsc()).thenReturn(List.of(addon));

        assertThat(service.list(null, false)).containsExactly(addon);
    }

    @Test
    void listFiltersByCategoryWhenGiven() {
        var addon = AddonInstance.builder().id(1L).name("Vent cutout").category(AddonCategory.HVAC_CUTOUT).active(true).build();
        when(addons.findAllByActiveTrueAndCategoryOrderByNameAsc(AddonCategory.HVAC_CUTOUT)).thenReturn(List.of(addon));

        assertThat(service.list(AddonCategory.HVAC_CUTOUT, false)).containsExactly(addon);
    }

    @Test
    void listIncludesInactiveWhenRequested() {
        var active = AddonInstance.builder().id(1L).name("Blinds").active(true).build();
        var inactive = AddonInstance.builder().id(2L).name("Old addon").active(false).build();
        when(addons.findAll()).thenReturn(List.of(active, inactive));

        assertThat(service.list(null, true)).containsExactly(active, inactive);
    }

    @Test
    void getThrowsWhenMissing() {
        when(addons.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(9L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void createDefaultsCategoryToOtherAndTrimsName() {
        var req = new AddonRequest("  Custom hook  ", null, BigDecimal.TEN, 15);
        when(addons.save(any(AddonInstance.class))).thenAnswer(inv -> inv.getArgument(0));

        var created = service.create(req);

        assertThat(created.getName()).isEqualTo("Custom hook");
        assertThat(created.getCategory()).isEqualTo(AddonCategory.OTHER);
        assertThat(created.isActive()).isTrue();
    }

    @Test
    void updateOverwritesFields() {
        var existing = AddonInstance.builder().id(1L).name("Old").category(AddonCategory.OTHER)
                .cost(BigDecimal.ONE).installTimeMinutes(5).active(true).build();
        when(addons.findById(1L)).thenReturn(Optional.of(existing));
        when(addons.save(any(AddonInstance.class))).thenAnswer(inv -> inv.getArgument(0));

        var req = new AddonRequest("New name", AddonCategory.BLINDS_RAILING, BigDecimal.valueOf(20), 30);
        var updated = service.update(1L, req);

        assertThat(updated.getName()).isEqualTo("New name");
        assertThat(updated.getCategory()).isEqualTo(AddonCategory.BLINDS_RAILING);
        assertThat(updated.getCost()).isEqualByComparingTo(BigDecimal.valueOf(20));
        assertThat(updated.getInstallTimeMinutes()).isEqualTo(30);
    }

    @Test
    void deleteMarksInactiveInsteadOfRemoving() {
        var existing = AddonInstance.builder().id(1L).name("Blinds").active(true).build();
        when(addons.findById(1L)).thenReturn(Optional.of(existing));
        when(addons.save(any(AddonInstance.class))).thenAnswer(inv -> inv.getArgument(0));

        service.delete(1L);

        assertThat(existing.isActive()).isFalse();
        verify(addons).save(existing);
    }
}
