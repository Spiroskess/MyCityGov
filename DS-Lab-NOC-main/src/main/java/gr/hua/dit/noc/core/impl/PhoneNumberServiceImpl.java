package gr.hua.dit.noc.core.impl;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import gr.hua.dit.noc.core.PhoneNumberService;
import gr.hua.dit.noc.core.model.PhoneNumberValidationResult;
import org.springframework.stereotype.Service;

import java.util.Locale;

/**
 * libphonenumber-based implementation of {@link PhoneNumberService}.
 *
 * @author Dimitris Gkoulis
 */
@Service
public class PhoneNumberServiceImpl implements PhoneNumberService {

    private final PhoneNumberUtil phoneNumberUtil;
    private final String defaultRegion;

    public PhoneNumberServiceImpl() {
        this.phoneNumberUtil = PhoneNumberUtil.getInstance();
        this.defaultRegion = "GR";
    }

    @Override
    public PhoneNumberValidationResult validatePhoneNumber(final String rawPhoneNumber) {
        if (rawPhoneNumber == null) return PhoneNumberValidationResult.invalid(rawPhoneNumber);
        if (rawPhoneNumber.isBlank()) return PhoneNumberValidationResult.invalid(rawPhoneNumber);
        try {
            final Phonenumber.PhoneNumber phoneNumber = this.phoneNumberUtil.parse(rawPhoneNumber, this.defaultRegion);
            return PhoneNumberValidationResult.valid(
                    rawPhoneNumber,
                    this.phoneNumberUtil.getNumberType(phoneNumber).name().toLowerCase(Locale.ROOT),
                    this.phoneNumberUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164)
            );
        } catch (NumberParseException e) {
            return PhoneNumberValidationResult.invalid(rawPhoneNumber);
        }
    }
}
