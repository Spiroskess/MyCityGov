package gr.hua.dit.mycitygov.web.api.advice;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;

@RestControllerAdvice(basePackages = "gr.hua.dit.mycitygov.web.rest")
@Order(1)
public class GlobalErrorHandlerRestControllerAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalErrorHandlerRestControllerAdvice.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAnyError(final Exception exception,
                                                   final HttpServletRequest request) {

        // Global handler για REST: μετατρέπει exceptions σε ενιαίο JSON ApiError με σωστό HTTP status
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        if (exception instanceof NoResourceFoundException) {
            status = HttpStatus.NOT_FOUND;
        } else if (exception instanceof AuthenticationException || exception instanceof SecurityException) {
            status = HttpStatus.UNAUTHORIZED;
        } else if (exception instanceof AuthorizationDeniedException || exception instanceof AccessDeniedException) {
            status = HttpStatus.FORBIDDEN;
        } else if (exception instanceof IllegalArgumentException) {
            status = HttpStatus.BAD_REQUEST;
        } else if (exception instanceof ResponseStatusException responseStatusException) {
            try {
                status = HttpStatus.valueOf(responseStatusException.getStatusCode().value());
            } catch (Exception ignored) {}
        }

        LOGGER.warn("REST error [{} {}] -> status={} cause={}: {}",
            request.getMethod(),
            request.getRequestURI(),
            status.value(),
            exception.getClass().getSimpleName(),
            exception.getMessage()
        );

        ApiError apiError = new ApiError(
            Instant.now(),
            status.value(),
            status.getReasonPhrase(),
            exception.getMessage(),
            request.getRequestURI()
        );

        return ResponseEntity.status(status).body(apiError);
    }
}
