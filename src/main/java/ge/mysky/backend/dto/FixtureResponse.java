package ge.mysky.backend.dto;

import ge.mysky.backend.domain.Fixture;
import ge.mysky.backend.domain.FixtureUnit;
import java.math.BigDecimal;

public record FixtureResponse(
        Long id,
        String name,
        FixtureUnit unit,
        BigDecimal cost,
        BigDecimal installTimeMinutes,
        boolean active) {

    public static FixtureResponse from(Fixture f) {
        return new FixtureResponse(
                f.getId(), f.getName(), f.getUnit(), f.getCost(), f.getInstallTimeMinutes(), f.isActive());
    }
}
