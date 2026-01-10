package gr.hua.dit.noc.web.rest;

import gr.hua.dit.noc.core.SmsService;
import gr.hua.dit.noc.core.model.SendSmsRequest;
import gr.hua.dit.noc.core.model.SendSmsResult;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API Resource class for managing SMS (sends).
 *
 * @author Dimitris Gkoulis
 */
@RestController
@RequestMapping(value = "/api/v1/sms", produces = MediaType.APPLICATION_JSON_VALUE)
public class SmsResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmsResource.class);

    private final SmsService smsService;

    public SmsResource(final SmsService smsService) {
        if (smsService == null) throw new NullPointerException();
        this.smsService = smsService;
    }

    @PostMapping(value = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SendSmsResult> sendSms(@RequestBody @Valid SendSmsRequest sendSmsRequest) {
        final SendSmsResult sendSmsResult = this.smsService.send(sendSmsRequest);
        return ResponseEntity.ok(sendSmsResult);
    }
}
