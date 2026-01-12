package gr.hua.dit.mycitygov.core.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;

    public JwtAuthenticationFilter(final JwtService jwtService) {
        if (jwtService == null) throw new NullPointerException("jwtService is null");
        this.jwtService = jwtService;
    }

    private void writeError(final HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"invalid_token\"}");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        final String path = request.getServletPath();

        // Άφησε swagger/openapi/h2 + auth endpoints χωρίς JWT
        if (path.startsWith("/swagger-ui")) return true;
        if (path.startsWith("/v3/api-docs")) return true;
        if (path.startsWith("/h2-console")) return true;

        // άλλαξε αυτό στο δικό σου login endpoint:
        // π.χ. αν το login σου είναι POST /api/auth/login
        if (path.startsWith("/api/auth")) return true;

        // Αν δεν είναι /api/**, μην κάνεις filter (π.χ. HTML pages)
        return !path.startsWith("/api/");
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void doFilterInternal(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final FilterChain filterChain) throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        // No header ή όχι Bearer -> συνεχίζει unauthenticated
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authorizationHeader.substring(7);

        try {
            final Claims claims = this.jwtService.parse(token);

            final String subject = claims.getSubject(); // email
            final Collection<String> roles = (Collection<String>) claims.get("roles");

            final List<GrantedAuthority> authorities =
                (roles == null)
                    ? List.of()
                    : roles.stream()
                    .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                    .collect(Collectors.toList()); // Java 11 friendly

            final User principal = new User(subject, "", authorities);

            final UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception ex) {
            LOGGER.warn("JwtAuthenticationFilter failed", ex);
            writeError(response);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
