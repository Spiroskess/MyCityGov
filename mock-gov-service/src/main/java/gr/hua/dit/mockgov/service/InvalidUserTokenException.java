package gr.hua.dit.mockgov.service;

public class InvalidUserTokenException extends RuntimeException {
    public InvalidUserTokenException(String message) {
        super(message);
    }
}
