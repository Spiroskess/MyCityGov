package gr.hua.dit.mycitygov.core.port.impl.dto;

import java.time.Instant;

public record ProviderStatusDto(
    String provider,
    Instant now,
    long tokenTtlMinutes
) {}
