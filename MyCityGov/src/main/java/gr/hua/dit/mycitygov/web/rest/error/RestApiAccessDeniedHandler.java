package gr.hua.dit.mycitygov.web.rest.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.hua.dit.mycitygov.web.api.advice.ApiError;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

/**
 * REST API 403 handler returning JSON {@link ApiError}.
 */
@Component
public class RestApiAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public RestApiAccessDeniedHandler(final ObjectMapper objectMapper) {
        if (objectMapper == null) throw new NullPointerException();
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        final ApiError apiError = new ApiError(
            Instant.now(),
            HttpStatus.FORBIDDEN.value(),
            HttpStatus.FORBIDDEN.getReasonPhrase(),
            "",
            request.getRequestURI(),
            null
        );

        response.getWriter().write(objectMapper.writeValueAsString(apiError));
    }
}
