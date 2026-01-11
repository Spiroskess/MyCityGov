package gr.hua.dit.mycitygov.web.api.advice;

import java.time.Instant;

// DTO για standard JSON error response στο REST API
public record ApiError(
    Instant timestamp,
    int status,
    String error,
    String message,
    String path
) {}
