package gr.hua.dit.mycitygov.web.api.advice;

import java.time.Instant;

/**
 * Standard JSON error response for REST API.
 * "details" is optional (e.g., validation field errors).
 */
public record ApiError(
    Instant timestamp,
    int status,
    String error,
    String message,
    String path,
    Object details
) {}
