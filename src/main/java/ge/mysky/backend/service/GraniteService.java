package ge.mysky.backend.service;

import ge.mysky.backend.domain.GraniteConfig;
import ge.mysky.backend.dto.GraniteConfigRequest;
import ge.mysky.backend.repository.GraniteConfigRepository;
import ge.mysky.backend.web.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GraniteService {

    private final GraniteConfigRepository configs;

    public GraniteService(GraniteConfigRepository configs) {
        this.configs = configs;
    }

    @Transactional(readOnly = true)
    public GraniteConfig get() {
        return configs.findById(GraniteConfig.SINGLETON_ID)
                .orElseThrow(() -> new NotFoundException("Granite config not initialized"));
    }

    @Transactional
    public GraniteConfig update(GraniteConfigRequest req) {
        var config = get();
        config.setPricePerMeter(req.pricePerMeter());
        config.setTimePerMeterMinutes(req.timePerMeterMinutes());
        return configs.save(config);
    }
}
