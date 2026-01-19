package gr.hua.dit.mycitygov.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mycitygov.noc")
public record NocProperties(
    String baseUrl,
    Sms sms
) {
    public record Sms(boolean active, boolean failFast) {}
}

