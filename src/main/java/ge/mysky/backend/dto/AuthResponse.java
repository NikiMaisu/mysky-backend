package ge.mysky.backend.dto;

public record AuthResponse(
        String accessToken,
        long accessTokenExpiresInSeconds,
        String refreshToken,
        long refreshTokenExpiresInSeconds,
        UserResponse user) {
}
