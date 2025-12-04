// src/main/java/gr/hua/dit/mycitygov/core/service/mapper/RequestMapper.java
package gr.hua.dit.mycitygov.core.service.mapper;

import gr.hua.dit.mycitygov.core.model.Request;
import gr.hua.dit.mycitygov.core.model.RequestStatus;
import gr.hua.dit.mycitygov.core.service.model.RequestView;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.EnumSet;

@Component
public class RequestMapper {

    // τελικές καταστάσεις – αν φτάσει εδώ, δεν μας νοιάζει αν έχει λήξει η προθεσμία
    private static final EnumSet<RequestStatus> TERMINAL_STATUSES =
        EnumSet.of(RequestStatus.COMPLETED, RequestStatus.REJECTED);

    public RequestView convertRequestToView(Request request) {
        String citizenName = request.getCitizen().getLastName() + " " + request.getCitizen().getFirstName();
        String employeeName = request.getAssignedEmployee() == null
            ? null
            : request.getAssignedEmployee().getLastName() + " " + request.getAssignedEmployee().getFirstName();

        LocalDate slaDueDate = request.getSlaDueDate();

        boolean overdue = false;
        if (slaDueDate != null) {
            overdue = slaDueDate.isBefore(LocalDate.now())
                && !TERMINAL_STATUSES.contains(request.getStatus());
        }

        return new RequestView(
            request.getId(),
            request.getProtocolNumber(),
            request.getType(),
            request.getStatus(),
            request.getSubject(),
            citizenName,
            employeeName,
            request.getCreatedAt(),
            slaDueDate,
            overdue
        );
    }
}
