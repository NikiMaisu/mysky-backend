package ge.mysky.backend.config;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mysky")
public record MyskyProperties(
        Jwt jwt,
        Cors cors,
        Admin admin) {

    public record Jwt(
            String secret,
            long accessTokenTtlMinutes,
            long refreshTokenTtlDays,
            String issuer) {
    }

    public record Cors(
            List<String> allowedOrigins) {
    }

    public record Admin(
            String email,
            String name,
            String password) {
    }
}
