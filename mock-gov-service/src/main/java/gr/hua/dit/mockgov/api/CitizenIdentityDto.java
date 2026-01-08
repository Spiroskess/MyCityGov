package gr.hua.dit.mockgov.api;

/**
 * Response προς το MyCityGov: βασικά στοιχεία πολίτη.
 */
public record CitizenIdentityDto(
    String afm,
    String amka,
    String firstName,
    String lastName
) {}
