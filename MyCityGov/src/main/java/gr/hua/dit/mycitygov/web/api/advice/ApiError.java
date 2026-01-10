package gr.hua.dit.mycitygov.web.api.advice;

import java.time.Instant;

public record ApiError(
    Instant timestamp,
    int status,
    String error,
    String message,
    String path
) {}
