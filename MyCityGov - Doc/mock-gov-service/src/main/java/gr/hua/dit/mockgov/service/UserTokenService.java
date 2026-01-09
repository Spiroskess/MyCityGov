package gr.hua.dit.mockgov.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.hua.dit.mockgov.api.CitizenIdentityDto;
import gr.hua.dit.mockgov.config.MockGovProperties;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Service
public class UserTokenService {

    private static final String PREFIX = "UG"; // UserGov token prefix

    private final ObjectMapper objectMapper;
    private final MockGovProperties props;

    public UserTokenService(ObjectMapper objectMapper, MockGovProperties props) {
        this.objectMapper = objectMapper;
        this.props = props;
    }

    public String issue(CitizenIdentityDto identity) {
        long ttlSeconds = Math.max(60, props.tokenTtlMinutes() * 60);
        long exp = Instant.now().plusSeconds(ttlSeconds).getEpochSecond();

        Payload payload = new Payload(identity.afm(), identity.amka(), identity.firstName(), identity.lastName(), exp);

        String payloadJson;
        try {
            payloadJson = objectMapper.writeValueAsString(payload);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot serialize payload", e);
        }

        String payloadB64 = base64Url(payloadJson.getBytes(StandardCharsets.UTF_8));
        String sigB64 = base64Url(hmacSha256((PREFIX + "." + payloadB64).getBytes(StandardCharsets.UTF_8)));

        return PREFIX + "." + payloadB64 + "." + sigB64;
    }

    public CitizenIdentityDto validate(String userToken) {
        if (userToken == null || userToken.isBlank()) {
            throw new InvalidUserTokenException("Missing user token");
        }

        String[] parts = userToken.trim().split("\\.");
        if (parts.length != 3 || !PREFIX.equals(parts[0])) {
            throw new InvalidUserTokenException("Bad token format");
        }

        String payloadB64 = parts[1];
        String sigB64 = parts[2];

        String expectedSig = base64Url(hmacSha256((PREFIX + "." + payloadB64).getBytes(StandardCharsets.UTF_8)));
        if (!constantTimeEquals(expectedSig, sigB64)) {
            throw new InvalidUserTokenException("Bad token signature");
        }

        Payload payload;
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(payloadB64);
            payload = objectMapper.readValue(decoded, Payload.class);
        } catch (Exception e) {
            throw new InvalidUserTokenException("Unreadable token");
        }

        long now = Instant.now().getEpochSecond();
        if (payload.exp == null || payload.exp < now) {
            throw new InvalidUserTokenException("Token expired");
        }

        return new CitizenIdentityDto(payload.afm, payload.amka, payload.firstName, payload.lastName);
    }

    private byte[] hmacSha256(byte[] data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(props.hmacSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return mac.doFinal(data);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot compute HMAC", e);
        }
    }

    private String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        byte[] x = a.getBytes(StandardCharsets.UTF_8);
        byte[] y = b.getBytes(StandardCharsets.UTF_8);
        if (x.length != y.length) return false;
        int diff = 0;
        for (int i = 0; i < x.length; i++) diff |= (x[i] ^ y[i]);
        return diff == 0;
    }

    private static final class Payload {
        public String afm;
        public String amka;
        public String firstName;
        public String lastName;
        public Long exp;

        public Payload() {}

        public Payload(String afm, String amka, String firstName, String lastName, Long exp) {
            this.afm = afm;
            this.amka = amka;
            this.firstName = firstName;
            this.lastName = lastName;
            this.exp = exp;
        }
    }
}
