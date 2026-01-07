package gr.hua.dit.mycitygov.govauth;

import gr.hua.dit.mycitygov.govauth.dto.CitizenIdentityDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class GovAuthClient {

    private final RestTemplate restTemplate;
    private final String validateUrl;

    public GovAuthClient(RestTemplate restTemplate,
                         @Value("${mycitygov.govauth.validate-url}") String validateUrl) {
        this.restTemplate = restTemplate;
        this.validateUrl = validateUrl;
    }

    public CitizenIdentityDto validateToken(String userToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(userToken.trim());

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<CitizenIdentityDto> response = restTemplate.exchange(
                validateUrl,
                HttpMethod.POST,
                request,
                CitizenIdentityDto.class
            );

            return response.getBody();

        } catch (HttpClientErrorException.Unauthorized ex) {
            throw new GovAuthException("Μη έγκυρο token.");
        }
    }
}
