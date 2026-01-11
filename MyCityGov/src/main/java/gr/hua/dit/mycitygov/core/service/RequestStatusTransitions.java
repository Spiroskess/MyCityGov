package gr.hua.dit.mycitygov.core.service;

import gr.hua.dit.mycitygov.core.model.RequestStatus;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class RequestStatusTransitions {

    // State machine: επιτρεπτές μεταβάσεις κατάστασης αιτήματος
    private static final Map<RequestStatus, Set<RequestStatus>> ALLOWED = Map.of(
        RequestStatus.SUBMITTED, EnumSet.of(RequestStatus.RECEIVED, RequestStatus.REJECTED),
        RequestStatus.RECEIVED, EnumSet.of(RequestStatus.IN_PROGRESS, RequestStatus.REJECTED),
        RequestStatus.IN_PROGRESS, EnumSet.of(
            RequestStatus.WAITING_ADDITIONAL_INFO,
            RequestStatus.COMPLETED,
            RequestStatus.REJECTED
        ),
        RequestStatus.WAITING_ADDITIONAL_INFO, EnumSet.of(
            RequestStatus.IN_PROGRESS,
            RequestStatus.REJECTED
        ),
        RequestStatus.COMPLETED, EnumSet.noneOf(RequestStatus.class),
        RequestStatus.REJECTED, EnumSet.noneOf(RequestStatus.class)
    );

    private RequestStatusTransitions() {}

    public static boolean canMove(final RequestStatus from, final RequestStatus to) {
        // Έλεγχος αν επιτρέπεται μετάβαση from -> to
        if (from == null || to == null) return false;
        return ALLOWED.getOrDefault(from, Set.of()).contains(to);
    }

    // Για να φτιάχνουμε κουμπιά UI με βάση το current status
    public static Set<RequestStatus> nextStatuses(final RequestStatus from) {
        if (from == null) return Set.of();
        Set<RequestStatus> next = ALLOWED.getOrDefault(from, Set.of());
        return next.isEmpty() ? Set.of() : Collections.unmodifiableSet(next);
    }
}
