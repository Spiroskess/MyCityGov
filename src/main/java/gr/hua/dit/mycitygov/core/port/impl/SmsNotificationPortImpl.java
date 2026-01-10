package gr.hua.dit.mycitygov.core.port.impl;

import gr.hua.dit.mycitygov.config.NocProperties;
import gr.hua.dit.mycitygov.core.port.SmsNotificationPort;
import gr.hua.dit.mycitygov.core.port.impl.dto.SendSmsRequest;
import gr.hua.dit.mycitygov.core.port.impl.dto.SendSmsResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
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
        LOGGER.error("SENDING SMS JSON e164={} content='{}'", e164, content);
        if (e164 == null || e164.isBlank()) throw new IllegalArgumentException("e164 is blank");
        if (content == null || content.isBlank()) throw new IllegalArgumentException("content is blank");

        if (!nocProperties.sms().active()) {
            LOGGER.warn("SMS notifications disabled (mycitygov.noc.sms.active=false). Would send to {}: {}", e164, content);
            return true;
        }

        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        final SendSmsRequest body = new SendSmsRequest(e164, content);
        final HttpEntity<SendSmsRequest> entity = new HttpEntity<>(body, headers);

        final String url = nocProperties.baseUrl() + "/api/v1/sms";
        final ResponseEntity<SendSmsResult> response =
            restTemplate.postForEntity(url, entity, SendSmsResult.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody().sent();
        }

        throw new IllegalStateException("NOC send sms failed with status: " + response.getStatusCode());
    }

}
