package ge.mysky.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** days[0]=Mon … days[6]=Sun; start/end as "HH:mm". */
public record WorkScheduleDto(
        @NotNull boolean[] days,
        @NotBlank String start,
        @NotBlank String end) {
}
