package ge.mysky.backend.dto;

import ge.mysky.backend.domain.Role;
import ge.mysky.backend.domain.User;

public record UserResponse(
        Long id,
        String name,
        String email,
        Role role) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}
