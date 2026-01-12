package gr.hua.dit.mycitygov.web.rest;

import gr.hua.dit.mycitygov.core.security.JwtService;
import gr.hua.dit.mycitygov.web.rest.model.LoginRequest;
import gr.hua.dit.mycitygov.web.rest.model.LoginResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthRestController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthRestController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {

        try {
            Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email, req.password)
            );

            // roles από authorities -> "ADMIN", "CITIZEN" κλπ (χωρίς ROLE_)
            List<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.startsWith("ROLE_") ? a.substring(5) : a)
                .toList();

            String token = jwtService.issue(req.email, roles);

            return ResponseEntity.ok(new LoginResponse(token));

        } catch (BadCredentialsException ex) {
            // λάθος email/password
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "bad_credentials"));
        } catch (AuthenticationException ex) {
            // οποιοδήποτε άλλο auth πρόβλημα
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "authentication_failed"));
        }
    }
}
