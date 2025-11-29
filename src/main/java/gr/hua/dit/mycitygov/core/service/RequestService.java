package gr.hua.dit.mycitygov.core.service;

import gr.hua.dit.mycitygov.core.model.Person;
import gr.hua.dit.mycitygov.core.service.model.OpenRequestRequest;
import gr.hua.dit.mycitygov.core.service.model.RequestView;

import java.util.List;
import java.util.Optional;

public interface RequestService {

    RequestView openRequest(Person citizen, OpenRequestRequest openRequestRequest);

    List<RequestView> getRequestsOfCitizen(Person citizen);

    List<RequestView> getRequestsAssignedToEmployee(Person employee);

    List<RequestView> getAllRequests();

    Optional<RequestView> assignRequestToEmployee(Long requestId, Person employee);
}
