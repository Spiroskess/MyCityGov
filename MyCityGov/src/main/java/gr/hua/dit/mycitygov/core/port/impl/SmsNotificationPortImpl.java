package gr.hua.dit.mycitygov.core.port.impl;

import gr.hua.dit.mycitygov.config.NocProperties;
import gr.hua.dit.mycitygov.core.port.SmsNotificationPort;
import gr.hua.dit.mycitygov.core.port.impl.dto.SendSmsRequest;
import gr.hua.dit.mycitygov.core.port.impl.dto.SendSmsResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class SmsNotificationPortImpl implements SmsNotificationPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmsNotificationPortImpl.class);

    private final RestTemplate restTemplate;
    private final NocProperties nocProperties;

    public SmsNotificationPortImpl(final RestTemplate restTemplate, final NocProperties nocProperties) {
        if (restTemplate == null) throw new NullPointerException();
        if (nocProperties == null) throw new NullPointerException();
        this.restTemplate = restTemplate;
        this.nocProperties = nocProperties;
    }

    @Override
    public boolean sendSms(final String e164, final String content) {
        // IMPORTANT: Η αποστολή SMS είναι side-effect και ΔΕΝ πρέπει να ρίχνει όλο το business flow.
        // Αν το NOC είναι down/απλήρωτο/επιστρέφει 5xx, απλώς κάνουμε log και συνεχίζουμε.
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Attempting to send SMS to e164={} (len={})", e164, content == null ? 0 : content.length());
        }

        if (e164 == null || e164.isBlank()) {
            LOGGER.warn("Skipping SMS send: missing/blank recipient number (e164)");
            return false;
        }
        if (content == null || content.isBlank()) {
            LOGGER.warn("Skipping SMS send: missing/blank content for recipient {}", e164);
            return false;
        }

        if (!nocProperties.sms().active()) {
            LOGGER.warn("SMS notifications disabled (mycitygov.noc.sms.active=false). Would send to {}: {}", e164, content);
            return true;
        }

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        final SendSmsRequest body = new SendSmsRequest(e164, content);
        final HttpEntity<SendSmsRequest> entity = new HttpEntity<>(body, headers);

        final String url = nocProperties.baseUrl() + "/api/v1/sms";
        try {
            final ResponseEntity<SendSmsResult> response =
                restTemplate.postForEntity(url, entity, SendSmsResult.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().sent();
            }

            final String msg = "NOC send sms returned non-success status: " + response.getStatusCode();
            if (nocProperties.sms().failFast()) {
                throw new IllegalStateException(msg);
            }
            LOGGER.warn("{} (non-blocking)", msg);
            return false;

        } catch (RestClientException ex) {
            // π.χ. 500 από NOC, connection refused, timeouts, DNS errors, κ.λπ.
            if (nocProperties.sms().failFast()) {
                throw ex;
            }
            LOGGER.warn("NOC SMS send failed (non-blocking): {}", ex.getMessage());
            LOGGER.debug("NOC SMS send stacktrace", ex);
            return false;
        }
    }
}
