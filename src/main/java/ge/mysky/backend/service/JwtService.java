package ge.mysky.backend.service;

import ge.mysky.backend.config.MyskyProperties;
import ge.mysky.backend.domain.Role;
import ge.mysky.backend.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TYPE = "typ";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final MyskyProperties props;
    private final SecretKey signingKey;

    public JwtService(MyskyProperties props) {
        this.props = props;
        var secretBytes = props.jwt().secret().getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException(
                    "mysky.jwt.secret must be at least 32 bytes (256 bits) for HS256");
        }
        this.signingKey = Keys.hmacShaKeyFor(secretBytes);
    }

    public String issueAccessToken(User user) {
        return buildToken(user, TYPE_ACCESS, Duration.ofMinutes(props.jwt().accessTokenTtlMinutes()));
    }

    public String issueRefreshToken(User user) {
        return buildToken(user, TYPE_REFRESH, Duration.ofDays(props.jwt().refreshTokenTtlDays()));
    }

    public ParsedToken parseAccessToken(String token) {
        return parse(token, TYPE_ACCESS);
    }

    public ParsedToken parseRefreshToken(String token) {
        return parse(token, TYPE_REFRESH);
    }

    public long accessTokenTtlSeconds() {
        return Duration.ofMinutes(props.jwt().accessTokenTtlMinutes()).toSeconds();
    }

    public long refreshTokenTtlSeconds() {
        return Duration.ofDays(props.jwt().refreshTokenTtlDays()).toSeconds();
    }

    private String buildToken(User user, String type, Duration ttl) {
        var now = Instant.now();
        return Jwts.builder()
                .issuer(props.jwt().issuer())
                .subject(user.getId().toString())
                .claim(CLAIM_ROLE, user.getRole().name())
                .claim(CLAIM_TYPE, type)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    private ParsedToken parse(String token, String expectedType) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .requireIssuer(props.jwt().issuer())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            var actualType = claims.get(CLAIM_TYPE, String.class);
            if (!expectedType.equals(actualType)) {
                throw new JwtException("Token type mismatch: expected " + expectedType + ", got " + actualType);
            }

            return new ParsedToken(
                    Long.parseLong(claims.getSubject()),
                    Role.valueOf(claims.get(CLAIM_ROLE, String.class)));
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException(e.getMessage(), e);
        }
    }

    public record ParsedToken(Long userId, Role role) {
    }

    public static class InvalidTokenException extends RuntimeException {
        public InvalidTokenException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
