package ge.mysky.backend.dto;

import ge.mysky.backend.domain.User;

public record WorkerResponse(
        Long id,
        String name,
        String email,
        boolean active) {

    public static WorkerResponse from(User u) {
        return new WorkerResponse(u.getId(), u.getName(), u.getEmail(), u.isActive());
    }
}
