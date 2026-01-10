package gr.hua.dit.mockgov.service;

public class UnauthorizedClientException extends RuntimeException {
    public UnauthorizedClientException(String message) {
        super(message);
    }
}
