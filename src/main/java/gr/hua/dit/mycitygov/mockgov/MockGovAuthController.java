package gr.hua.dit.mycitygov.mockgov;

import gr.hua.dit.mycitygov.mockgov.dto.CitizenIdentityDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/external-auth/api/v1")
public class MockGovAuthController {

    // Σταθερό mock mapping: token -> πολίτης
    private static final Map<String, CitizenIdentityDto> TOKENS = Map.of(
        "TOKEN-123", new CitizenIdentityDto("333333333", "33333333333", "LEFKOS", "CHARALAMBOUS"),
        "TOKEN-456", new CitizenIdentityDto("444444444", "44444444444", "NICOLAS", "CHARALAMBOUS")
    );

    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader
    ) {
        // Παίρνουμε το token από το header
        String token = extractBearerToken(authorizationHeader);

        if (token == null) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Missing or invalid Authorization header");
        }

        // Ψάχνουμε αν το token υπάρχει
        CitizenIdentityDto identity = TOKENS.get(token);

        if (identity == null) {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body("Invalid token");
        }

        // Αν είναι όλα καλά, επιστρέφουμε τα στοιχεία πολίτη
        return ResponseEntity.ok(identity);
    }

    private String extractBearerToken(String header) {
        if (header == null) return null;
        if (!header.startsWith("Bearer ")) return null;
        return header.substring(7).trim();
    }
}
