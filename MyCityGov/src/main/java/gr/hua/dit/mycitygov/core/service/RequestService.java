package gr.hua.dit.mycitygov.core.service;

import gr.hua.dit.mycitygov.core.model.Person;
import gr.hua.dit.mycitygov.core.model.RequestStatus;
import gr.hua.dit.mycitygov.core.service.model.MunicipalService;
import gr.hua.dit.mycitygov.core.service.model.OpenRequestRequest;
import gr.hua.dit.mycitygov.core.service.model.RequestMessageView;
import gr.hua.dit.mycitygov.core.service.model.RequestView;

import java.util.List;
import java.util.Optional;

// Service interface για business logic αιτημάτων: υποβολή, ανάθεση, status transitions, ιστορικό μηνυμάτων
public interface RequestService {

    RequestView openRequest(Person citizen, OpenRequestRequest openRequestRequest);

    List<RequestView> getRequestsOfCitizen(Person citizen);

    Optional<RequestView> getCitizenRequestDetails(Long requestId, Person citizen);
    List<RequestMessageView> getCitizenMessages(Long requestId, Person citizen);

    List<RequestView> getRequestsAssignedToEmployee(Person employee);

    List<RequestView> getAllRequests();

    List<RequestView> getUnassignedRequests();
    List<RequestView> getAssignedRequests();

    Optional<RequestView> assignRequestToService(Long requestId, MunicipalService service);

    List<RequestView> getServiceQueue(MunicipalService service);

    Optional<RequestView> claimRequest(Long requestId, Person employee);

    Optional<RequestView> updateStatus(
        Long requestId,
        Person employee,
        RequestStatus nextStatus,
        String comment
    );

    Optional<RequestView> getMyRequestDetails(Long requestId, Person employee);
    List<RequestMessageView> getMyRequestMessages(Long requestId, Person employee);

    void citizenSubmittedAdditionalInfo(Long requestId, Person citizen, int uploadedFilesCount, String note);
}
