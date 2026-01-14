package gr.hua.dit.mycitygov.web.rest;

import gr.hua.dit.mycitygov.core.security.CurrentUserProvider;
import gr.hua.dit.mycitygov.core.service.RequestService;
import gr.hua.dit.mycitygov.core.service.model.OpenRequestRequest;
import gr.hua.dit.mycitygov.core.service.model.RequestMessageView;
import gr.hua.dit.mycitygov.core.service.model.RequestView;
import gr.hua.dit.mycitygov.web.rest.model.CitizenAdditionalInfoRestRequest;
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
@RequestMapping(value = "/api/requests", produces = MediaType.APPLICATION_JSON_VALUE)
public class RequestRestController {

    private final RequestService requestService;
    private final CurrentUserProvider currentUserProvider;

    public RequestRestController(RequestService requestService, CurrentUserProvider currentUserProvider) {
        this.requestService = requestService;
        this.currentUserProvider = currentUserProvider;
    }

    // CITIZEN: open request
    @PreAuthorize("hasRole('CITIZEN')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public RequestView open(@Valid @RequestBody OpenRequestRequest request) {
        var citizen = currentUserProvider.requireCurrentPerson();
        return requestService.openRequest(citizen, request);
    }

    // CITIZEN: list my requests
    @PreAuthorize("hasRole('CITIZEN')")
    @GetMapping("/my")
    public List<RequestView> myRequests() {
        var citizen = currentUserProvider.requireCurrentPerson();
        return requestService.getRequestsOfCitizen(citizen);
    }

    // CITIZEN: request details
    @PreAuthorize("hasRole('CITIZEN')")
    @GetMapping("/{id}")
    public RequestView myRequestDetails(@PathVariable Long id) {
        var citizen = currentUserProvider.requireCurrentPerson();
        return requestService.getCitizenRequestDetails(id, citizen)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));
    }

    // CITIZEN: messages visible to citizen
    @PreAuthorize("hasRole('CITIZEN')")
    @GetMapping("/{id}/messages")
    public List<RequestMessageView> myRequestMessages(@PathVariable Long id) {
        var citizen = currentUserProvider.requireCurrentPerson();
        return requestService.getCitizenMessages(id, citizen);
    }

    // CITIZEN: submit additional info (note + "uploadedFilesCount" as metadata)
    @PreAuthorize("hasRole('CITIZEN')")
    @PostMapping(value = "/{id}/additional-info", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void additionalInfo(@PathVariable Long id, @Valid @RequestBody CitizenAdditionalInfoRestRequest request) {
        var citizen = currentUserProvider.requireCurrentPerson();
        requestService.citizenSubmittedAdditionalInfo(id, citizen, request.uploadedFilesCount, request.note);
    }
}
