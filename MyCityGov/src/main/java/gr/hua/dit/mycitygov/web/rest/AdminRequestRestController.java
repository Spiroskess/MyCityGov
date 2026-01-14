package gr.hua.dit.mycitygov.web.rest;

import gr.hua.dit.mycitygov.core.security.CurrentUserProvider;
import gr.hua.dit.mycitygov.core.service.RequestService;
import gr.hua.dit.mycitygov.core.service.model.RequestView;
import gr.hua.dit.mycitygov.web.rest.model.AdminRequestStatsDto;
import gr.hua.dit.mycitygov.web.rest.model.AssignRequestToServiceRequest;
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
@RequestMapping(value = "/api/admin/requests", produces = MediaType.APPLICATION_JSON_VALUE)
public class AdminRequestRestController {

    private final RequestService requestService;
    private final CurrentUserProvider currentUserProvider;

    public AdminRequestRestController(RequestService requestService, CurrentUserProvider currentUserProvider) {
        this.requestService = requestService;
        this.currentUserProvider = currentUserProvider;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<RequestView> list(@RequestParam(name = "view", defaultValue = "all") String view) {
        currentUserProvider.requireAdminId();

        return switch (view) {
            case "unassigned" -> requestService.getUnassignedRequests();
            case "assigned" -> requestService.getAssignedRequests();
            default -> requestService.getAllRequests();
        };
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping(value = "/{id}/assign-service", consumes = MediaType.APPLICATION_JSON_VALUE)
    public RequestView assignToService(@PathVariable Long id, @Valid @RequestBody AssignRequestToServiceRequest request) {
        currentUserProvider.requireAdminId();

        return requestService.assignRequestToService(id, request.service)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/stats")
    public AdminRequestStatsDto stats() {
        currentUserProvider.requireAdminId();

        var all = requestService.getAllRequests();
        long total = all.size();
        long unassigned = all.stream().filter(r -> r.assignedService() == null).count();
        long assigned = total - unassigned;
        long overdue = all.stream().filter(RequestView::overdue).count();

        return new AdminRequestStatsDto(total, unassigned, assigned, overdue);
    }
}
