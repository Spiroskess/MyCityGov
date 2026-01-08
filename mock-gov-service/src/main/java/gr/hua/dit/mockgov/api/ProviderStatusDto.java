package gr.hua.dit.mockgov.api;

import java.time.Instant;

public record ProviderStatusDto(
    String provider,
    Instant now,
    long tokenTtlMinutes
) {}
