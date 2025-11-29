package gr.hua.dit.mycitygov.core.service.mapper;


import gr.hua.dit.mycitygov.core.model.Request;
import gr.hua.dit.mycitygov.core.service.model.RequestView;
import org.springframework.stereotype.Component;

@Component
public class RequestMapper {

    public RequestView convertRequestToView(Request request) {
        String citizenName = request.getCitizen().getLastName() + " " + request.getCitizen().getFirstName();
        String employeeName = request.getAssignedEmployee() == null
            ? null
            : request.getAssignedEmployee().getLastName() + " " + request.getAssignedEmployee().getFirstName();

        return new RequestView(
            request.getId(),
            request.getProtocolNumber(),
            request.getType(),
            request.getStatus(),
            request.getSubject(),
            citizenName,
            employeeName,
            request.getCreatedAt()
        );
    }
}

