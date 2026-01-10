package gr.hua.dit.mockgov.web.api;

import gr.hua.dit.mockgov.api.CitizenIdentityDto;
import gr.hua.dit.mockgov.api.IssueTokenRequest;
import gr.hua.dit.mockgov.api.IssueTokenResponse;
import gr.hua.dit.mockgov.api.ProviderStatusDto;
import gr.hua.dit.mockgov.api.ValidateTokenRequest;
import gr.hua.dit.mockgov.config.MockGovProperties;
import gr.hua.dit.mockgov.repository.CitizenDirectory;
import gr.hua.dit.mockgov.service.CitizenNotFoundException;
import gr.hua.dit.mockgov.service.ClientAuthService;
import gr.hua.dit.mockgov.service.UserTokenService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

/**
 * REST API του MockGov που καλεί server-to-server το MyCityGov.
 * Όλα τα endpoints είναι secured με Authorization: Bearer <clientToken>.
 */
@RestController
@RequestMapping("/external-auth/api/v1")
public class ExternalAuthApiController {

    private final ClientAuthService clientAuthService;
    private final CitizenDirectory citizenDirectory;
    private final UserTokenService userTokenService;
    private final MockGovProperties props;

    public ExternalAuthApiController(ClientAuthService clientAuthService,
                                     CitizenDirectory citizenDirectory,
                                     UserTokenService userTokenService,
                                     MockGovProperties props) {
        this.clientAuthService = clientAuthService;
        this.citizenDirectory = citizenDirectory;
        this.userTokenService = userTokenService;
        this.props = props;
    }

    /**
     * GET ping endpoint (χρησιμοποιείται από το MyCityGov για "providerOk").
     */
    @GetMapping("/status")
    public ProviderStatusDto status(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        clientAuthService.requireValidClientAuthorization(authorization);
        return new ProviderStatusDto("MockGov", Instant.now(), props.tokenTtlMinutes());
    }

    /**
     * POST issue userToken από credentials (optional flow).
     */
    @PostMapping("/issue")
    public IssueTokenResponse issue(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody IssueTokenRequest request) {

        clientAuthService.requireValidClientAuthorization(authorization);

        CitizenIdentityDto citizen = citizenDirectory
                .findByCredentials(request.afm(), request.amka(), request.lastName())
                .orElseThrow(() -> new CitizenNotFoundException("Citizen not found"));

        String userToken = userTokenService.issue(citizen);
        return new IssueTokenResponse(userToken);
    }

    /**
     * POST validate userToken -> επιστρέφει βασικά στοιχεία πολίτη.
     */
    @PostMapping("/validate")
    public CitizenIdentityDto validate(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody ValidateTokenRequest request) {

        clientAuthService.requireValidClientAuthorization(authorization);
        return userTokenService.validate(request.userToken());
    }
}
