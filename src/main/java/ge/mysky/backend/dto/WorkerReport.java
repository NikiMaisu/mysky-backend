package ge.mysky.backend.dto;

import java.math.BigDecimal;

public record WorkerReport(
        Long workerId,
        String name,
        long orderCount,
        long totalMinutes,
        BigDecimal totalSquareMeters) {
}
