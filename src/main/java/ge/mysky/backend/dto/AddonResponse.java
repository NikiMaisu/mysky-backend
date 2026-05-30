package ge.mysky.backend.dto;

import ge.mysky.backend.domain.AddonCategory;
import ge.mysky.backend.domain.AddonInstance;
import java.math.BigDecimal;

public record AddonResponse(
        Long id,
        String name,
        AddonCategory category,
        BigDecimal cost,
        Integer installTimeMinutes,
        boolean active) {

    public static AddonResponse from(AddonInstance a) {
        return new AddonResponse(
                a.getId(), a.getName(), a.getCategory(), a.getCost(), a.getInstallTimeMinutes(), a.isActive());
    }
}
