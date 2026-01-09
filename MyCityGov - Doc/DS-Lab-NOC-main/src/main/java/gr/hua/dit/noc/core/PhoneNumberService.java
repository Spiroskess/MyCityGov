package gr.hua.dit.noc.core;

import gr.hua.dit.noc.core.model.PhoneNumberValidationResult;

/**
 * Service for managing phone numbers.
 *
 * @author Dimitris Gkoulis
 */
public interface PhoneNumberService {

    PhoneNumberValidationResult validatePhoneNumber(final String rawPhoneNumber);
}
