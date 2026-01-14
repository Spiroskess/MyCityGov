package gr.hua.dit.mycitygov.web.rest.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import gr.hua.dit.mycitygov.web.api.advice.ApiError;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

/**
 * REST API 401 handler returning JSON {@link ApiError}.
 */
@Component
public class RestApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public RestApiAuthenticationEntryPoint(final ObjectMapper objectMapper) {
        if (objectMapper == null) throw new NullPointerException();
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        final ApiError apiError = new ApiError(
            Instant.now(),
            HttpStatus.UNAUTHORIZED.value(),
            HttpStatus.UNAUTHORIZED.getReasonPhrase(),
            "",
            request.getRequestURI(),
            null
        );

        response.getWriter().write(objectMapper.writeValueAsString(apiError));
    }
}
