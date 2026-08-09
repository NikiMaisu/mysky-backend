package ge.mysky.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ge.mysky.backend.domain.Fixture;
import ge.mysky.backend.domain.FixtureUnit;
import ge.mysky.backend.dto.FixtureRequest;
import ge.mysky.backend.repository.FixtureRepository;
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
class FixtureServiceTest {

    @Mock
    private FixtureRepository fixtures;

    private FixtureService service;

    @BeforeEach
    void setUp() {
        service = new FixtureService(fixtures);
    }

    @Test
    void listReturnsActiveOnlyByDefault() {
        var fixture = Fixture.builder().id(1L).name("Spotlight").active(true).build();
        when(fixtures.findAllByActiveTrueOrderByNameAsc()).thenReturn(List.of(fixture));

        assertThat(service.list(false)).containsExactly(fixture);
    }

    @Test
    void listIncludesInactiveWhenRequested() {
        var active = Fixture.builder().id(1L).name("Spotlight").active(true).build();
        var inactive = Fixture.builder().id(2L).name("Old fixture").active(false).build();
        when(fixtures.findAll()).thenReturn(List.of(active, inactive));

        assertThat(service.list(true)).containsExactly(active, inactive);
    }

    @Test
    void getThrowsWhenMissing() {
        when(fixtures.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(9L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void createTrimsNameAndSetsActive() {
        var req = new FixtureRequest("  Spotlight  ", FixtureUnit.PER_UNIT, BigDecimal.valueOf(50), BigDecimal.valueOf(10));
        when(fixtures.save(any(Fixture.class))).thenAnswer(inv -> inv.getArgument(0));

        var created = service.create(req);

        assertThat(created.getName()).isEqualTo("Spotlight");
        assertThat(created.getUnit()).isEqualTo(FixtureUnit.PER_UNIT);
        assertThat(created.isActive()).isTrue();
    }

    @Test
    void updateOverwritesFields() {
        var existing = Fixture.builder().id(1L).name("Old").unit(FixtureUnit.PER_UNIT)
                .cost(BigDecimal.ONE).installTimeMinutes(BigDecimal.ONE).active(true).build();
        when(fixtures.findById(1L)).thenReturn(Optional.of(existing));
        when(fixtures.save(any(Fixture.class))).thenAnswer(inv -> inv.getArgument(0));

        var req = new FixtureRequest("New name", FixtureUnit.PER_METER, BigDecimal.valueOf(75), BigDecimal.valueOf(20));
        var updated = service.update(1L, req);

        assertThat(updated.getName()).isEqualTo("New name");
        assertThat(updated.getUnit()).isEqualTo(FixtureUnit.PER_METER);
        assertThat(updated.getCost()).isEqualByComparingTo(BigDecimal.valueOf(75));
        assertThat(updated.getInstallTimeMinutes()).isEqualByComparingTo(BigDecimal.valueOf(20));
    }

    @Test
    void deleteMarksInactiveInsteadOfRemoving() {
        var existing = Fixture.builder().id(1L).name("Spotlight").active(true).build();
        when(fixtures.findById(1L)).thenReturn(Optional.of(existing));
        when(fixtures.save(any(Fixture.class))).thenAnswer(inv -> inv.getArgument(0));

        service.delete(1L);

        assertThat(existing.isActive()).isFalse();
        verify(fixtures).save(existing);
    }
}
