package ge.mysky.backend.controller;

import ge.mysky.backend.dto.AuthResponse;
import ge.mysky.backend.dto.LoginRequest;
import ge.mysky.backend.dto.RefreshRequest;
import ge.mysky.backend.dto.UserResponse;
import ge.mysky.backend.repository.UserRepository;
import ge.mysky.backend.service.JwtService;
import ge.mysky.backend.service.MyskyUserDetailsService.MyskyUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwt;
    private final UserRepository users;

    public AuthController(AuthenticationManager authManager, JwtService jwt, UserRepository users) {
        this.authManager = authManager;
        this.jwt = jwt;
        this.users = users;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        try {
            var auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.identifier(), req.password()));
            var principal = (MyskyUserDetails) auth.getPrincipal();
            return buildResponse(principal);
        } catch (BadCredentialsException e) {
            throw new ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest req) {
        try {
            var parsed = jwt.parseRefreshToken(req.refreshToken());
            var user = users.findById(parsed.userId())
                    .orElseThrow(() -> new ResponseStatusException(
                            org.springframework.http.HttpStatus.UNAUTHORIZED, "User no longer exists"));
            return buildResponse(new MyskyUserDetails(user));
        } catch (JwtService.InvalidTokenException e) {
            throw new ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token");
        }
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public UserResponse me(@AuthenticationPrincipal MyskyUserDetails principal) {
        return UserResponse.from(principal.user());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<String> handleAuth(AuthenticationException e) {
        return ResponseEntity.status(401).body(e.getMessage());
    }

    private AuthResponse buildResponse(MyskyUserDetails principal) {
        var user = principal.user();
        return new AuthResponse(
                jwt.issueAccessToken(user),
                jwt.accessTokenTtlSeconds(),
                jwt.issueRefreshToken(user),
                jwt.refreshTokenTtlSeconds(),
                UserResponse.from(user));
    }
}
