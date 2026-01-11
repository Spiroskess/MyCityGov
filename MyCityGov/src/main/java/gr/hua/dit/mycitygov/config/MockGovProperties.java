package gr.hua.dit.mycitygov.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

// Ρυθμίσεις MockGov από το application.yml (urls + client token για authentication).
@ConfigurationProperties(prefix = "mycitygov.mockgov")
public record MockGovProperties(
    String apiBaseUrl,

    String publicBaseUrl,
    String clientToken
) {}
