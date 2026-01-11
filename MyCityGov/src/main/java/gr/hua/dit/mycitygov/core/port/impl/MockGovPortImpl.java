package gr.hua.dit.mycitygov.core.port.impl;

import gr.hua.dit.mycitygov.config.MockGovProperties;
import gr.hua.dit.mycitygov.core.port.MockGovPort;
import gr.hua.dit.mycitygov.core.port.impl.dto.*;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

@Service
public class MockGovPortImpl implements MockGovPort {

    private final RestTemplate restTemplate;
    private final MockGovProperties props;

    public MockGovPortImpl(RestTemplate restTemplate, MockGovProperties props) {
        this.restTemplate = restTemplate;
        this.props = props;
    }

    @Override
    public ProviderStatusDto status() {
        // GET προς external MockGov service για health/status
        String url = props.apiBaseUrl() + "/external-auth/api/v1/status";
        HttpEntity<Void> entity = new HttpEntity<>(authHeaders());

        try {
            ResponseEntity<ProviderStatusDto> resp =
                restTemplate.exchange(url, HttpMethod.GET, entity, ProviderStatusDto.class);

            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                return resp.getBody();
            }
            throw new IllegalStateException("MockGov status failed: " + resp.getStatusCode());
        } catch (RestClientResponseException ex) {
            throw new IllegalStateException("MockGov status error: " + ex.getRawStatusCode(), ex);
        }
    }

    @Override
    public String issueUserToken(String afm, String amka, String lastName) {
        // POST προς external MockGov για έκδοση user token
        String url = props.apiBaseUrl() + "/external-auth/api/v1/issue";
        HttpEntity<IssueTokenRequest> entity =
            new HttpEntity<>(new IssueTokenRequest(afm, amka, lastName), authHeaders());

        try {
            ResponseEntity<IssueTokenResponse> resp =
                restTemplate.postForEntity(url, entity, IssueTokenResponse.class);

            if (resp.getStatusCode().is2xxSuccessful()
                && resp.getBody() != null
                && resp.getBody().userToken() != null
                && !resp.getBody().userToken().isBlank()) {
                return resp.getBody().userToken();
            }
            throw new IllegalStateException("MockGov issue failed: " + resp.getStatusCode());
        } catch (RestClientResponseException ex) {
            throw new IllegalArgumentException("Αποτυχία ταυτοποίησης (issue): " + ex.getRawStatusCode(), ex);
        }
    }

    @Override
    public CitizenIdentityDto validateUserToken(String userToken) {
        // POST προς external MockGov για έλεγχο/επαλήθευση token
        String url = props.apiBaseUrl() + "/external-auth/api/v1/validate";
        HttpEntity<ValidateTokenRequest> entity =
            new HttpEntity<>(new ValidateTokenRequest(userToken), authHeaders());

        try {
            ResponseEntity<CitizenIdentityDto> resp =
                restTemplate.postForEntity(url, entity, CitizenIdentityDto.class);

            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                return resp.getBody();
            }
            throw new IllegalStateException("MockGov validate failed: " + resp.getStatusCode());
        } catch (RestClientResponseException ex) {
            throw new IllegalArgumentException("Μη έγκυρο gov token (validate): " + ex.getRawStatusCode(), ex);
        }
    }

    private HttpHeaders authHeaders() {
        // Authorization header για secured external API calls (Bearer client token)
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.set(HttpHeaders.AUTHORIZATION, "Bearer " + props.clientToken());
        return h;
    }
}
