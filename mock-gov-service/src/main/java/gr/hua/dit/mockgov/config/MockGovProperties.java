package gr.hua.dit.mockgov.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mockgov")
public record MockGovProperties(
    String clientToken,
    String hmacSecret,
    long tokenTtlMinutes
) {}
