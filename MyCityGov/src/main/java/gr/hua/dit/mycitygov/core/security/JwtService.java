package gr.hua.dit.mycitygov.core.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;

@Service
public class JwtService {

    private final Key key;
    private final String issuer;
    private final String audience;
    private final long ttlMinutes;

    public JwtService(
        @Value("${security.jwt.secret}") final String secret,
        @Value("${security.jwt.issuer:mycitygov}") final String issuer,
        @Value("${security.jwt.audience:mycitygov-api}") final String audience,
        @Value("${security.jwt.ttlMinutes:120}") final long ttlMinutes
    ) {
        if (secret == null) throw new NullPointerException("security.jwt.secret is null");
        if (secret.isBlank()) throw new IllegalArgumentException("security.jwt.secret is blank");
        if (secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("security.jwt.secret must be >= 32 bytes");
        }
        if (issuer == null || issuer.isBlank()) throw new IllegalArgumentException("security.jwt.issuer is blank");
        if (audience == null || audience.isBlank()) throw new IllegalArgumentException("security.jwt.audience is blank");
        if (ttlMinutes <= 0) throw new IllegalArgumentException("security.jwt.ttlMinutes must be > 0");

        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.audience = audience;
        this.ttlMinutes = ttlMinutes;
    }

    /** Όπως στο video: issue token */
    public String issue(final String subject, final Collection<String> roles) {
        final Instant now = Instant.now();
        return Jwts.builder()
            .setSubject(subject) // email
            .setIssuer(this.issuer)
            .setAudience(this.audience)
            .claim("roles", roles)
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(now.plus(Duration.ofMinutes(this.ttlMinutes))))
            .signWith(this.key, SignatureAlgorithm.HS256)
            .compact();
    }

    /** Όπως στο video: parse token -> claims (throws exception αν invalid) */
    public Claims parse(final String token) {
        return Jwts.parserBuilder()
            .requireIssuer(this.issuer)
            .requireAudience(this.audience)
            .setSigningKey(this.key)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
}
