package gr.hua.dit.mycitygov.core.port.impl.dto;

// DTO απάντησης από MockGov - βασικά στοιχεία πολίτη μετά από validate token
public record CitizenIdentityDto(
    String afm,
    String amka,
    String firstName,
    String lastName
) {}
