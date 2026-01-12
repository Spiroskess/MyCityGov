package gr.hua.dit.mycitygov.config;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;
import java.security.Key;

@Configuration
public class JwtConfig {

    @Bean
    public Key jwtSigningKey(@Value("${security.jwt.secret}") String secret) {
        // Για απλό dev secret ως string:
        // Προσοχή: θέλει αρκετό μήκος για HS256.
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        // Αν θες base64 secret, χρησιμοποίησε:
        // byte[] keyBytes = Decoders.BASE64.decode(secret);
        // return Keys.hmacShaKeyFor(keyBytes);
    }
}
