package gr.hua.dit.noc.core.model;

/**
 * PhoneNumberValidationResult DTO.
 *
 * @author Dimitris Gkoulis
 * @see gr.hua.dit.noc.core.PhoneNumberService
 */
public record PhoneNumberValidationResult(
        String raw,
        boolean valid,
        String type,
        String e164
) {

    public static PhoneNumberValidationResult invalid(final String raw) {
        return new PhoneNumberValidationResult(raw, false, null, null);
    }

    public static PhoneNumberValidationResult valid(final String raw, final String type, final String e164) {
        return new PhoneNumberValidationResult(raw, true, type, e164);
    }
}
