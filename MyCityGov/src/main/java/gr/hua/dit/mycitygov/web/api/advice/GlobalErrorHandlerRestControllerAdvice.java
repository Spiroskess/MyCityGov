package gr.hua.dit.mycitygov.web.api.advice;

import gr.hua.dit.mycitygov.core.service.exception.InvalidRequestStatusTransitionException;
import gr.hua.dit.mycitygov.core.service.exception.RequestNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice(basePackages = "gr.hua.dit.mycitygov.web.rest")
@Order(1)
public class GlobalErrorHandlerRestControllerAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalErrorHandlerRestControllerAdvice.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(final MethodArgumentNotValidException ex,
                                                     final HttpServletRequest request) {

        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(fe.getField(), fe.getDefaultMessage());
        }

        ApiError apiError = new ApiError(
            Instant.now(),
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "Validation failed",
            request.getRequestURI(),
            fieldErrors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(final ConstraintViolationException ex,
                                                              final HttpServletRequest request) {

        Map<String, String> violations = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(v -> violations.put(
            v.getPropertyPath().toString(),
            v.getMessage()
        ));

        ApiError apiError = new ApiError(
            Instant.now(),
            HttpStatus.BAD_REQUEST.value(),
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "Validation failed",
            request.getRequestURI(),
            violations
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiError);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAnyError(final Exception exception,
                                                   final HttpServletRequest request) {

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        if (exception instanceof NoResourceFoundException) {
            status = HttpStatus.NOT_FOUND;

        } else if (exception instanceof RequestNotFoundException) {
            // FIX: “not found / not assigned” -> 404 (όχι 409)
            status = HttpStatus.NOT_FOUND;

        } else if (exception instanceof NoSuchElementException) {
            status = HttpStatus.NOT_FOUND;

        } else if (exception instanceof InvalidRequestStatusTransitionException) {
            // FIX: invalid transition -> 409 με καθαρό μήνυμα
            status = HttpStatus.CONFLICT;

        } else if (exception instanceof AuthenticationException || exception instanceof SecurityException) {
            status = HttpStatus.UNAUTHORIZED;

        } else if (exception instanceof AuthorizationDeniedException || exception instanceof AccessDeniedException) {
            status = HttpStatus.FORBIDDEN;

        } else if (exception instanceof IllegalStateException) {
            // Γενικά business conflicts (π.χ. REQUEST_NOT_WAITING_ADDITIONAL_INFO)
            status = HttpStatus.CONFLICT;

        } else if (exception instanceof MethodArgumentTypeMismatchException
            || exception instanceof ConversionFailedException) {
            status = HttpStatus.BAD_REQUEST;

        } else if (exception instanceof IllegalArgumentException) {
            status = HttpStatus.BAD_REQUEST;

        } else if (exception instanceof ResponseStatusException responseStatusException) {
            try {
                status = HttpStatus.valueOf(responseStatusException.getStatusCode().value());
            } catch (Exception ignored) {
                // keep INTERNAL_SERVER_ERROR
            }
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
            request.getRequestURI(),
            null
        );

        return ResponseEntity.status(status).body(apiError);
    }
}
