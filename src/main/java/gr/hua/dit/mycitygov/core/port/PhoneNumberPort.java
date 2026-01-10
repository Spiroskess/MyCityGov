package gr.hua.dit.mycitygov.core.port;

import gr.hua.dit.mycitygov.core.port.impl.dto.PhoneNumberValidationResult;

public interface PhoneNumberPort {
    PhoneNumberValidationResult validate(String rawPhoneNumber);
}
