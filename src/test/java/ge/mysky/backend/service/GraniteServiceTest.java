package ge.mysky.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import ge.mysky.backend.domain.GraniteConfig;
import ge.mysky.backend.dto.GraniteConfigRequest;
import ge.mysky.backend.repository.GraniteConfigRepository;
import ge.mysky.backend.web.NotFoundException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GraniteServiceTest {

    @Mock
    private GraniteConfigRepository configs;

    private GraniteService service;

    @BeforeEach
    void setUp() {
        service = new GraniteService(configs);
    }

    @Test
    void getThrowsWhenNotInitialized() {
        when(configs.findById(GraniteConfig.SINGLETON_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(service::get).isInstanceOf(NotFoundException.class);
    }

    @Test
    void getReturnsTheSingletonRow() {
        var config = new GraniteConfig(GraniteConfig.SINGLETON_ID, BigDecimal.TEN, BigDecimal.ONE, OffsetDateTime.now());
        when(configs.findById(GraniteConfig.SINGLETON_ID)).thenReturn(Optional.of(config));

        assertThat(service.get()).isSameAs(config);
    }

    @Test
    void updateOverwritesPriceAndTime() {
        var config = new GraniteConfig(GraniteConfig.SINGLETON_ID, BigDecimal.TEN, BigDecimal.ONE, OffsetDateTime.now());
        when(configs.findById(GraniteConfig.SINGLETON_ID)).thenReturn(Optional.of(config));
        when(configs.save(any(GraniteConfig.class))).thenAnswer(inv -> inv.getArgument(0));

        var req = new GraniteConfigRequest(BigDecimal.valueOf(120), BigDecimal.valueOf(5));
        var updated = service.update(req);

        assertThat(updated.getPricePerMeter()).isEqualByComparingTo(BigDecimal.valueOf(120));
        assertThat(updated.getTimePerMeterMinutes()).isEqualByComparingTo(BigDecimal.valueOf(5));
    }
}
