package gr.hua.dit.mycitygov.web.ui;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class UiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException ex, Model model, HttpServletResponse response) {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        model.addAttribute("status", HttpStatus.BAD_REQUEST.value());
        model.addAttribute("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        model.addAttribute("message", ex.getMessage());
        return "error";
    }
}
