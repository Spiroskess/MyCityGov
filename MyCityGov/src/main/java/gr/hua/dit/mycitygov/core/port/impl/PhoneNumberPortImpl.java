package gr.hua.dit.mycitygov.core.port.impl;

import gr.hua.dit.mycitygov.config.NocProperties;
import gr.hua.dit.mycitygov.core.port.PhoneNumberPort;
import gr.hua.dit.mycitygov.core.port.impl.dto.PhoneNumberValidationResult;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class PhoneNumberPortImpl implements PhoneNumberPort {

    private final RestTemplate restTemplate;
    private final NocProperties nocProperties;

    public PhoneNumberPortImpl(final RestTemplate restTemplate, final NocProperties nocProperties) {
        if (restTemplate == null) throw new NullPointerException();
        if (nocProperties == null) throw new NullPointerException();
        this.restTemplate = restTemplate;
        this.nocProperties = nocProperties;
    }

    @Override
    public PhoneNumberValidationResult validate(final String rawPhoneNumber) {
        // GET προς external NOC service για validation τηλεφώνου
        final String url = nocProperties.baseUrl()
            + "/api/v1/phone-numbers/" + rawPhoneNumber + "/validations";

        final ResponseEntity<PhoneNumberValidationResult> response =
            restTemplate.getForEntity(url, PhoneNumberValidationResult.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody();
        }

        throw new IllegalStateException("NOC phone validation failed with status: " + response.getStatusCode());
    }
}
