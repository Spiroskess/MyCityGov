package gr.hua.dit.mycitygov.web.ui;

import gr.hua.dit.mycitygov.core.model.Person;
import gr.hua.dit.mycitygov.core.model.RequestStatus;
import gr.hua.dit.mycitygov.core.security.CurrentUserProvider;
import gr.hua.dit.mycitygov.core.service.RequestAttachmentService;
import gr.hua.dit.mycitygov.core.service.RequestService;
import gr.hua.dit.mycitygov.core.service.model.MunicipalService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class EmployeeRequestController {

    private final RequestService requestService;
    private final CurrentUserProvider currentUserProvider;
    private final RequestAttachmentService requestAttachmentService;

    public EmployeeRequestController(RequestService requestService,
                                     CurrentUserProvider currentUserProvider,
                                     RequestAttachmentService requestAttachmentService) {
        this.requestService = requestService;
        this.currentUserProvider = currentUserProvider;
        this.requestAttachmentService = requestAttachmentService;
    }

    @GetMapping("/employee/requests")
    public String employeeRequestsIndex() {
        // Default redirect για employee requests -> ουρά υπηρεσίας
        return "redirect:/employee/requests-service";
    }

    @GetMapping("/employee/requests-service")
    public String serviceQueue(Model model) {
        // Employee UI - ουρά αιτημάτων της υπηρεσίας του (unclaimed)
        Person employee = currentUserProvider.getCurrentPerson().orElseThrow();
        MunicipalService service = employee.getMunicipalService();

        model.addAttribute("service", service);
        model.addAttribute("requests", requestService.getServiceQueue(service));
        return "employee/requests-service";
    }

    @GetMapping("/employee/requests-mine")
    public String myRequests(Model model) {
        // Employee UI - τα αιτήματα που έχουν ανατεθεί στον συγκεκριμένο υπάλληλο
        Person employee = currentUserProvider.getCurrentPerson().orElseThrow();
        model.addAttribute("requests", requestService.getRequestsAssignedToEmployee(employee));
        return "employee/requests-mine";
    }

    @GetMapping("/employee/requests/{id}")
    public String requestDetails(@PathVariable Long id,
                                 @RequestParam(required = false) String err,
                                 Model model) {
        // Employee UI - details αιτήματος
        Person employee = currentUserProvider.getCurrentPerson().orElseThrow();

        var opt = requestService.getMyRequestDetails(id, employee);
        if (opt.isEmpty()) {
            return "redirect:/employee/requests-mine";
        }

        model.addAttribute("r", opt.get());
        model.addAttribute("messages", requestService.getMyRequestMessages(id, employee));
        model.addAttribute("err", err);

        // attachments που έχει ανεβάσει ο πολίτης
        model.addAttribute("attachments", requestAttachmentService.listForEmployeeRequest(id, employee));

        return "employee/request-details";
    }

    @GetMapping("/employee/requests/{requestId}/attachments/{attachmentId}/download")
    @ResponseBody
    public ResponseEntity<Resource> downloadEmployeeAttachment(@PathVariable Long requestId,
                                                               @PathVariable Long attachmentId) {
        // Download συνημμένου
        Person employee = currentUserProvider.getCurrentPerson().orElseThrow();
        var dl = requestAttachmentService.downloadForEmployee(requestId, attachmentId, employee);

        String filename = (dl.originalFilename() == null) ? "attachment" : dl.originalFilename();
        String safe = URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");

        MediaType ct;
        try {
            ct = MediaType.parseMediaType(dl.contentType());
        } catch (Exception e) {
            ct = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + safe)
            .contentType(ct)
            .contentLength(dl.sizeBytes())
            .body(new InputStreamResource(dl.inputStream()));
    }

    @PostMapping("/employee/requests/claim")
    public String claim(@RequestParam Long requestId) {
        // “Ανάληψη” αιτήματος από υπάλληλο
        Person employee = currentUserProvider.getCurrentPerson().orElseThrow();
        requestService.claimRequest(requestId, employee);
        return "redirect:/employee/requests-service";
    }

    @PostMapping("/employee/requests/status")
    public String updateStatus(@RequestParam Long requestId,
                               @RequestParam RequestStatus nextStatus,
                               @RequestParam(required = false) String comment,
                               @RequestParam(required = false) String redirectTo) {
        // Αλλαγή status αιτήματος από υπάλληλο
        Person employee = currentUserProvider.getCurrentPerson().orElseThrow();

        try {
            requestService.updateStatus(requestId, employee, nextStatus, comment);
        } catch (IllegalArgumentException ex) {
            if ("COMMENT_REQUIRED".equals(ex.getMessage())) {
                return "redirect:/employee/requests/" + requestId + "?err=commentRequired";
            }
            throw ex;
        }

        String target = "/employee/requests-mine";
        if (redirectTo != null && redirectTo.startsWith("/employee/requests/")) {
            target = redirectTo;
        }
        return "redirect:" + target;
    }
}
