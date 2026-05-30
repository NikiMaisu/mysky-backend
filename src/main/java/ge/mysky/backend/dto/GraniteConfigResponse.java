package ge.mysky.backend.dto;

import ge.mysky.backend.domain.GraniteConfig;
import java.math.BigDecimal;

public record GraniteConfigResponse(
        BigDecimal pricePerMeter,
        BigDecimal timePerMeterMinutes) {

    public static GraniteConfigResponse from(GraniteConfig c) {
        return new GraniteConfigResponse(c.getPricePerMeter(), c.getTimePerMeterMinutes());
    }
}
