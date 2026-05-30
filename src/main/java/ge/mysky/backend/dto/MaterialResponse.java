package ge.mysky.backend.dto;

import ge.mysky.backend.domain.Material;
import java.math.BigDecimal;

public record MaterialResponse(
        Long id,
        String name,
        BigDecimal pricePerM2,
        BigDecimal timePerM2Minutes,
        boolean active) {

    public static MaterialResponse from(Material m) {
        return new MaterialResponse(
                m.getId(), m.getName(), m.getPricePerM2(), m.getTimePerM2Minutes(), m.isActive());
    }
}
