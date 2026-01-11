package gr.hua.dit.mycitygov.core.port.impl.dto;

// DTO request προς MockGov για έκδοση user token
public record IssueTokenRequest(
    String afm,
    String amka,
    String lastName
) {}
