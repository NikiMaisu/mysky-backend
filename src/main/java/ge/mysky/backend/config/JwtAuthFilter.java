package ge.mysky.backend.config;

import ge.mysky.backend.service.JwtService;
import ge.mysky.backend.service.MyskyUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwt;
    private final MyskyUserDetailsService userDetails;

    public JwtAuthFilter(JwtService jwt, MyskyUserDetailsService userDetails) {
        this.jwt = jwt;
        this.userDetails = userDetails;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        var header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            chain.doFilter(request, response);
            return;
        }

        var token = header.substring(BEARER_PREFIX.length());
        try {
            var parsed = jwt.parseAccessToken(token);
            var details = userDetails.loadUserById(parsed.userId());

            var auth = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (JwtService.InvalidTokenException | UsernameNotFoundException ignored) {
            // Leave the security context empty — downstream auth rules will reject if needed.
        }

        chain.doFilter(request, response);
    }
}
