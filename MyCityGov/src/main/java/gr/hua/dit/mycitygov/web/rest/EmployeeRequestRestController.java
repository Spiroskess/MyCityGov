package gr.hua.dit.mycitygov.web.rest;

import gr.hua.dit.mycitygov.core.security.CurrentUserProvider;
import gr.hua.dit.mycitygov.core.service.RequestService;
import gr.hua.dit.mycitygov.core.service.model.RequestMessageView;
import gr.hua.dit.mycitygov.core.service.model.RequestView;
import gr.hua.dit.mycitygov.web.rest.model.UpdateRequestStatusRestRequest;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping(value = "/api/employee/requests", produces = MediaType.APPLICATION_JSON_VALUE)
public class EmployeeRequestRestController {

    private final RequestService requestService;
    private final CurrentUserProvider currentUserProvider;

    public EmployeeRequestRestController(RequestService requestService, CurrentUserProvider currentUserProvider) {
        this.requestService = requestService;
        this.currentUserProvider = currentUserProvider;
    }

    // EMPLOYEE: list requests assigned to me
    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/my")
    public List<RequestView> myAssigned() {
        var employee = currentUserProvider.requireCurrentPerson();
        return requestService.getRequestsAssignedToEmployee(employee);
    }

    // EMPLOYEE: service queue (assignedService == my service, assignedEmployee == null)
    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/queue")
    public List<RequestView> serviceQueue() {
        var employee = currentUserProvider.requireCurrentPerson();
        if (employee.getMunicipalService() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "EMPLOYEE_HAS_NO_SERVICE");
        }
        return requestService.getServiceQueue(employee.getMunicipalService());
    }

    // EMPLOYEE: claim a request
    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/{id}/claim")
    public RequestView claim(@PathVariable Long id) {
        var employee = currentUserProvider.requireCurrentPerson();
        return requestService.claimRequest(id, employee)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot claim request"));
    }

    // EMPLOYEE: my request details (only if assigned to me)
    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/{id}")
    public RequestView myRequestDetails(@PathVariable Long id) {
        var employee = currentUserProvider.requireCurrentPerson();
        return requestService.getMyRequestDetails(id, employee)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Request not found or not assigned to you"
            ));
    }

    // EMPLOYEE: messages of my request
    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/{id}/messages")
    public List<RequestMessageView> myRequestMessages(@PathVariable Long id) {
        var employee = currentUserProvider.requireCurrentPerson();
        // Το service πλέον πετάει RequestNotFoundException -> 404 από GlobalErrorHandler
        return requestService.getMyRequestMessages(id, employee);
    }

    // EMPLOYEE: update status of my request
    @PreAuthorize("hasRole('EMPLOYEE')")
    @PatchMapping(value = "/{id}/status", consumes = MediaType.APPLICATION_JSON_VALUE)
    public RequestView updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateRequestStatusRestRequest request) {
        var employee = currentUserProvider.requireCurrentPerson();

        // Guard μόνο για το “assigned σε μένα” ώστε να ξεχωρίζει 403 (authorization)
        requestService.getMyRequestDetails(id, employee)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Cannot update status (not assigned to you)"
            ));

        // Το service θα:
        // - πετάξει InvalidRequestStatusTransitionException -> 409 (Global handler)
        // - πετάξει IllegalArgumentException (COMMENT_REQUIRED) -> 400 (Global handler)
        // - ενημερώσει SMS / δημιουργήσει messages / save
        return requestService.updateStatus(id, employee, request.status, request.comment)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Cannot update status"
            ));
    }
}
