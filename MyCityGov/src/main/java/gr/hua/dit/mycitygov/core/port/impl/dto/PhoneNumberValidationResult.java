package gr.hua.dit.mycitygov.core.port.impl.dto;

public record PhoneNumberValidationResult(
    String raw,
    boolean valid,
    String type,
    String e164
) {
    // DTO απάντησης από NOC για έλεγχο τηλεφώνου
    public boolean isValidMobile() {
        return valid && type != null && type.equalsIgnoreCase("mobile") && e164 != null && !e164.isBlank();
    }
}
