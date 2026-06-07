package ge.mysky.backend.dto;

import java.math.BigDecimal;

public record BrigadeReport(
        Long teamId,
        String teamName,
        long orderCount,
        long totalMinutes,
        BigDecimal totalSquareMeters,
        BigDecimal totalCost) {
}
