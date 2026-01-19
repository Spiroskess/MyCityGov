package gr.hua.dit.mycitygov.core.service.exception;

import gr.hua.dit.mycitygov.core.model.RequestStatus;

import java.util.Set;

public class InvalidRequestStatusTransitionException extends RuntimeException {

    private final RequestStatus from;
    private final RequestStatus to;
    private final Set<RequestStatus> allowed;

    public InvalidRequestStatusTransitionException(RequestStatus from, RequestStatus to, Set<RequestStatus> allowed) {
        super("Invalid status transition: " + from + " -> " + to + ". Allowed: " + allowed);
        this.from = from;
        this.to = to;
        this.allowed = allowed;
    }

    public RequestStatus getFrom() {
        return from;
    }

    public RequestStatus getTo() {
        return to;
    }

    public Set<RequestStatus> getAllowed() {
        return allowed;
    }
}
