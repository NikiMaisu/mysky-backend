package ge.mysky.backend.dto;

import ge.mysky.backend.domain.Order;
import ge.mysky.backend.domain.OrderStatus;
import java.time.OffsetDateTime;

public record CalendarOrder(
        Long id,
        Long orderNumber,
        String clientName,
        String address,
        Long teamId,
        String teamName,
        OffsetDateTime startAt,
        OffsetDateTime finishAt,
        OrderStatus status,
        int totalMinutes) {

    public static CalendarOrder from(Order o) {
        return new CalendarOrder(
                o.getId(), o.getOrderNumber(), o.getClientName(), o.getAddress(),
                o.getTeamId(), o.getTeamName(), o.getStartAt(), o.getFinishAt(),
                o.getStatus(), o.getTotalMinutes());
    }
}
