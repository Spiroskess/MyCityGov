package gr.hua.dit.mycitygov.core.port.impl.dto;

public record IssueTokenRequest(
    String afm,
    String amka,
    String lastName
) {}
