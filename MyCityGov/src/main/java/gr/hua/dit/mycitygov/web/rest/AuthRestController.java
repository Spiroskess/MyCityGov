package gr.hua.dit.mycitygov.web.rest;

import gr.hua.dit.mycitygov.core.model.PersonRole;
import gr.hua.dit.mycitygov.core.security.JwtService;
import gr.hua.dit.mycitygov.core.service.PersonService;
import gr.hua.dit.mycitygov.core.service.model.CreatePersonRequest;
import gr.hua.dit.mycitygov.core.service.model.CreatePersonResult;
import gr.hua.dit.mycitygov.web.rest.model.LoginRequest;
import gr.hua.dit.mycitygov.web.rest.model.LoginResponse;
import gr.hua.dit.mycitygov.web.rest.model.RegisterCitizenRequest;
import gr.hua.dit.mycitygov.web.rest.model.RegisterResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Tag(name = "0 - Auth")
@RestController
@RequestMapping(value = "/api/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthRestController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final PersonService personService;

    public AuthRestController(AuthenticationManager authenticationManager, JwtService jwtService, PersonService personService) {
        if (authenticationManager == null) throw new NullPointerException();
        if (jwtService == null) throw new NullPointerException();
        if (personService == null) throw new NullPointerException();

        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.personService = personService;
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {

        try {
            Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email, req.password)
            );

            List<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.startsWith("ROLE_") ? a.substring(5) : a)
                .toList();

            String token = jwtService.issue(req.email, roles);
            return new LoginResponse(token);

        } catch (BadCredentialsException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
    }

    /**
     * REST Registration (Citizen).
     * Returns 201 + JWT token so a SPA/mobile client can continue immediately.
     */
    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse registerCitizen(@Valid @RequestBody RegisterCitizenRequest req) {

        CreatePersonRequest createPersonRequest = new CreatePersonRequest(
            PersonRole.CITIZEN,
            req.email,
            req.firstName,
            req.lastName,
            req.mobilePhoneNumber,
            req.afm,
            req.amka,
            req.password
        );

        CreatePersonResult result = personService.createPerson(createPersonRequest, true);
        if (!result.created()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, result.reason());
        }

        String token = jwtService.issue(result.personView().emailAddress(), List.of("CITIZEN"));
        return new RegisterResponse(token, result.personView());
    }
}
