package gr.hua.dit.mycitygov.web.ui;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Global UI error handler: μετατρέπει exceptions σε φιλικές error σελίδες (error / error/404).
 */
@ControllerAdvice(basePackages = "gr.hua.dit.mycitygov.web")
@Order(2)
public class UiExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(UiExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public String handleAnyError(final Exception exception,
                                 final HttpServletRequest request,
                                 final HttpServletResponse response,
                                 final Model model) {

        // Χαρτογραφεί exceptions -> HTTP status (π.χ. 404/400/401/403/500) για σωστό UI response
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        if (exception instanceof NoResourceFoundException) {
            status = HttpStatus.NOT_FOUND;
        } else if (exception instanceof ResponseStatusException responseStatusException) {
            try {
                status = HttpStatus.valueOf(responseStatusException.getStatusCode().value());
            } catch (Exception ignored) {}
        } else if (exception instanceof MethodArgumentTypeMismatchException) {
            status = HttpStatus.BAD_REQUEST;
        } else if (exception instanceof IllegalArgumentException) {
            status = HttpStatus.BAD_REQUEST;
        } else if (exception instanceof AuthenticationException || exception instanceof SecurityException) {
            status = HttpStatus.UNAUTHORIZED;
        } else if (exception instanceof AuthorizationDeniedException || exception instanceof AccessDeniedException) {
            status = HttpStatus.FORBIDDEN;
        }

        response.setStatus(status.value());

        LOGGER.warn("UI error [{} {}] -> status={} cause={}: {}",
            request.getMethod(),
            request.getRequestURI(),
            status.value(),
            exception.getClass().getSimpleName(),
            exception.getMessage()
        );

        // Στέλνει βασικά στοιχεία στο template (error.html / 404.html)
        model.addAttribute("status", status.value());
        model.addAttribute("error", status.getReasonPhrase());
        model.addAttribute("message", exception.getMessage());
        model.addAttribute("path", request.getRequestURI());

        if (status == HttpStatus.NOT_FOUND) {
            return "error/404";
        }

        return "error";
    }
}
