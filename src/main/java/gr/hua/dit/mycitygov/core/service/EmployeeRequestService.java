package gr.hua.dit.mycitygov.core.service;

import gr.hua.dit.mycitygov.core.service.model.EmployeeRequestView;
import gr.hua.dit.mycitygov.core.service.model.UpdateRequestStatusRequest;

import java.util.List;

public interface EmployeeRequestService {

    // Όλα τα αιτήματα που μπορεί να δει ο υπάλληλος (προς το παρόν: όλα)
    List<EmployeeRequestView> getAllRequests();

    // Αιτήματα που έχει αναλάβει ο συγκεκριμένος υπάλληλος
    List<EmployeeRequestView> getAssignedRequests(String employeeEmail);

    // Ο υπάλληλος αναλαμβάνει το αίτημα
    void assignToSelf(Long requestId, String employeeEmail);

    // Ο υπάλληλος αλλάζει status + σχόλιο
    void updateStatus(Long requestId, String employeeEmail, UpdateRequestStatusRequest dto);
}
