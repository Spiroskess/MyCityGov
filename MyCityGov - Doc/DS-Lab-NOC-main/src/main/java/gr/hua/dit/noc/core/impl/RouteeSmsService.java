package gr.hua.dit.noc.core.impl;

import gr.hua.dit.noc.config.RouteeProperties;
import gr.hua.dit.noc.core.SmsService;
import gr.hua.dit.noc.core.model.SendSmsRequest;
import gr.hua.dit.noc.core.model.SendSmsResult;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * AMD Routee implementation of {@link SmsService}.
 *
 * @author Dimitris Gkoulis
 */
@Service
public class RouteeSmsService implements SmsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RouteeSmsService.class);

    private static final String AUTHENTICATION_URL = "https://auth.routee.net/oauth/token";
    private static final String SMS_URL = "https://connect.routee.net/sms";

    private final RestTemplate restTemplate;
    private final RouteeProperties routeeProperties;

    public RouteeSmsService(final RestTemplate restTemplate, final RouteeProperties routeeProperties) {
        if (restTemplate == null) throw new NullPointerException();
        if (routeeProperties == null) throw new NullPointerException();

        this.restTemplate = restTemplate;
        this.routeeProperties = routeeProperties;
    }

    // TODO Not working.
    @SuppressWarnings("rawtypes")
    @Cacheable("routeeAccessToken")
    public String getAccessToken() {
        LOGGER.info("Requesting Routee Access Token");

        final String credentials = this.routeeProperties.getAppId() + ":" + this.routeeProperties.getAppSecret();
        final String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", "Basic " + encoded);
        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        final HttpEntity<String> request = new HttpEntity<>("grant_type=client_credentials", httpHeaders);

        final ResponseEntity<Map> response =
                this.restTemplate.exchange(AUTHENTICATION_URL, HttpMethod.POST, request, Map.class);

        return (String) response.getBody().get("access_token");
    }

    @Override
    public SendSmsResult send(@Valid final SendSmsRequest sendSmsRequest) {
        if (sendSmsRequest == null) throw new NullPointerException();

        // --------------------------------------------------

        final String e164 = sendSmsRequest.e164();
        final String content = sendSmsRequest.content();

        // --------------------------------------------------

        if (e164 == null) throw new NullPointerException();
        if (e164.isBlank()) throw new IllegalArgumentException();
        if (content == null) throw new NullPointerException();
        if (content.isBlank()) throw new IllegalArgumentException();

        // Authenticate
        // --------------------------------------------------

        final String token = this.getAccessToken(); // TODO Handle error.

        // Headers
        // --------------------------------------------------

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", "Bearer " + token);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        // Payload
        // --------------------------------------------------

        final Map<String, Object> body = Map.of(
                "body", content,
                "to", e164,
                "from", this.routeeProperties.getSender());

        // Request
        // --------------------------------------------------

        final HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, httpHeaders);
        final ResponseEntity<String> response = this.restTemplate.postForEntity(SMS_URL, entity, String.class);

        LOGGER.info("Routee response: {}", response);

        if (!response.getStatusCode().is2xxSuccessful()) {
            LOGGER.error("SMS send to {} failed", e164);
            return new SendSmsResult(false);
        }

        return new SendSmsResult(true);
    }
}
