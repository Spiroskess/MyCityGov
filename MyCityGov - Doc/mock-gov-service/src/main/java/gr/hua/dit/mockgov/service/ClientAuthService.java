package gr.hua.dit.mockgov.service;

import gr.hua.dit.mockgov.config.MockGovProperties;
import org.springframework.stereotype.Service;

@Service
public class ClientAuthService {

    private final MockGovProperties props;

    public ClientAuthService(MockGovProperties props) {
        this.props = props;
    }

    public void requireValidClientAuthorization(String authorizationHeader) {
        String token = extractBearer(authorizationHeader);
        if (token == null || token.isBlank() || !token.equals(props.clientToken())) {
            throw new UnauthorizedClientException("Invalid client token");
        }
    }

    private String extractBearer(String header) {
        if (header == null) return null;
        String h = header.trim();
        if (h.regionMatches(true, 0, "Bearer ", 0, "Bearer ".length())) {
            return h.substring("Bearer ".length()).trim();
        }
        return null;
    }
}
