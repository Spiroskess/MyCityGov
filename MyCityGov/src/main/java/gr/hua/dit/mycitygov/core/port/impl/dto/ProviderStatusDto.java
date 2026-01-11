package gr.hua.dit.mycitygov.core.port.impl.dto;

import java.time.Instant;

// DTO status/health από MockGov - δείχνει ότι το external service είναι online
public record ProviderStatusDto(
    String provider,
    Instant now,
    long tokenTtlMinutes
) {}
