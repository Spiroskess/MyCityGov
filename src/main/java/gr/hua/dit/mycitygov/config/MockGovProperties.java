package gr.hua.dit.mycitygov.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mycitygov.mockgov")
public record MockGovProperties(
    String apiBaseUrl,

    String publicBaseUrl,
    String clientToken
) {}
