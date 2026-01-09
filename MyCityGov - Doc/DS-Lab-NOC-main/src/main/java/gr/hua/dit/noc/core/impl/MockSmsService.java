package gr.hua.dit.noc.core.impl;

import gr.hua.dit.noc.core.SmsService;
import gr.hua.dit.noc.core.model.SendSmsRequest;
import gr.hua.dit.noc.core.model.SendSmsResult;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Mock {@link SmsService}.
 */
@Service
public class MockSmsService implements SmsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockSmsService.class);

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

        // --------------------------------------------------

        LOGGER.info("SENDING SMS {} {}", e164, content);

        // --------------------------------------------------

        return new SendSmsResult(false);
    }
}
