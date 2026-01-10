package gr.hua.dit.mockgov.web;

import gr.hua.dit.mockgov.api.ApiError;
import gr.hua.dit.mockgov.service.CitizenNotFoundException;
import gr.hua.dit.mockgov.service.InvalidUserTokenException;
import gr.hua.dit.mockgov.service.UnauthorizedClientException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice(basePackages = "gr.hua.dit.mockgov.web.api")
public class ApiExceptionHandler {

    @ExceptionHandler(UnauthorizedClientException.class)
    public ResponseEntity<ApiError> unauthorizedClient(UnauthorizedClientException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiError.of("unauthorized_client", "Unauthorized client"));
    }

    @ExceptionHandler(InvalidUserTokenException.class)
    public ResponseEntity<ApiError> invalidToken(InvalidUserTokenException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ApiError.of("invalid_user_token", ex.getMessage()));
    }

    @ExceptionHandler(CitizenNotFoundException.class)
    public ResponseEntity<ApiError> notFound(CitizenNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiError.of("citizen_not_found", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
            .forEach(fe -> fieldErrors.put(fe.getField(), fe.getDefaultMessage()));

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("code", "validation_error");
        body.put("message", "Invalid request");
        body.put("errors", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> generic(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiError.of("server_error", "Unexpected error"));
    }
}
