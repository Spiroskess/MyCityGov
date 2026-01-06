package gr.hua.dit.mycitygov.govauth;

import gr.hua.dit.mycitygov.mockgov.dto.CitizenIdentityDto;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class GovAuthClient {

    private final RestTemplate restTemplate;

    public GovAuthClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public CitizenIdentityDto validateToken(String userToken) {
        // Η external υπηρεσία τρέχει στο 8080
        String url = "http://localhost:8080/external-auth/api/v1/validate";

        // Header: Authorization: Bearer <token>
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken.trim());

        // Δεν στέλνουμε body, μόνο headers
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<CitizenIdentityDto> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                CitizenIdentityDto.class
            );

            return response.getBody();

        } catch (HttpClientErrorException.Unauthorized ex) {
            // External service είπε "401"
            throw new GovAuthException("Μη έγκυρο token.");
        }
    }
}
