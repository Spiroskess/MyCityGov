package gr.hua.dit.mycitygov.web.ui;

import gr.hua.dit.mycitygov.core.model.Person;
import gr.hua.dit.mycitygov.core.model.RequestStatus;
import gr.hua.dit.mycitygov.core.security.CurrentUserProvider;
import gr.hua.dit.mycitygov.core.service.RequestAttachmentService;
import gr.hua.dit.mycitygov.core.service.RequestService;
import gr.hua.dit.mycitygov.core.service.model.AttachmentUpload;
import gr.hua.dit.mycitygov.core.service.model.OpenRequestRequest;

import jakarta.validation.Valid;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class CitizenRequestController {

    private final RequestService requestService;
    private final CurrentUserProvider currentUserProvider;
    private final RequestAttachmentService requestAttachmentService;

    public CitizenRequestController(RequestService requestService,
                                    CurrentUserProvider currentUserProvider,
                                    RequestAttachmentService requestAttachmentService) {
        this.requestService = requestService;
        this.currentUserProvider = currentUserProvider;
        this.requestAttachmentService = requestAttachmentService;
    }

    @GetMapping("/citizen/requests")
    public String listCitizenRequests(Model model) {
        Person citizen = currentUserProvider.getCurrentPerson().orElseThrow();

        var all = requestService.getRequestsOfCitizen(citizen);
        var active = all.stream()
            .filter(r -> !isCompletedStatus(r.status()))
            .toList();

        model.addAttribute("requests", active);
        return "citizen/requests";
    }

    @GetMapping("/citizen/requests/completed")
    public String listCitizenCompletedRequests(Model model) {
        Person citizen = currentUserProvider.getCurrentPerson().orElseThrow();

        var all = requestService.getRequestsOfCitizen(citizen);
        var completed = all.stream()
            .filter(r -> isCompletedStatus(r.status()))
            .toList();

        model.addAttribute("requests", completed);
        return "citizen/requests-completed";
    }

    @GetMapping("/citizen/request-new")
    public String showNewRequestForm(Model model) {
        model.addAttribute("openRequestRequest", new OpenRequestRequest(null, "", ""));
        return "citizen/request-new";
    }

    @PostMapping("/citizen/request-new")
    public String handleNewRequest(
        @Valid @ModelAttribute("openRequestRequest") OpenRequestRequest openRequestRequest,
        BindingResult bindingResult,
        @RequestParam(name = "attachments", required = false) MultipartFile[] attachments,
        Model model) {

        if (bindingResult.hasErrors()) {
            return "citizen/request-new";
        }

        Person citizen = currentUserProvider.getCurrentPerson().orElseThrow();

        // δημιουργία αιτήματος
        var created = requestService.openRequest(citizen, openRequestRequest);

        // upload + αποθήκευση metadata στη DB
        if (attachments != null) {
            for (MultipartFile f : attachments) {
                if (f == null || f.isEmpty()) continue;

                try {
                    var upload = new AttachmentUpload(
                        f.getOriginalFilename(),
                        f.getContentType(),
                        f.getSize(),
                        f.getInputStream()
                    );
                    requestAttachmentService.addForCitizenRequest(created.id(), citizen, upload);
                } catch (Exception e) {
                    String name = (f.getOriginalFilename() == null) ? "file" : f.getOriginalFilename();
                    model.addAttribute("uploadError", "Αποτυχία ανεβάσματος αρχείου: " + name);
                    return "citizen/request-new";
                }
            }
        }

        return "redirect:/citizen/requests";
    }

    @GetMapping("/citizen/requests/{id}")
    public String citizenRequestDetails(@PathVariable Long id, Model model) {
        Person citizen = currentUserProvider.getCurrentPerson().orElseThrow();

        var opt = requestService.getCitizenRequestDetails(id, citizen);
        if (opt.isEmpty()) {
            return "redirect:/citizen/requests";
        }

        model.addAttribute("r", opt.get());
        model.addAttribute("messages", requestService.getCitizenMessages(id, citizen));
        model.addAttribute("attachments", requestAttachmentService.listForCitizenRequest(id, citizen));

        return "citizen/request-details";
    }

    // Download συνημμένου από πολίτη
    @GetMapping("/citizen/requests/{requestId}/attachments/{attachmentId}/download")
    @ResponseBody
    public ResponseEntity<Resource> downloadCitizenAttachment(@PathVariable Long requestId,
                                                              @PathVariable Long attachmentId) {

        Person citizen = currentUserProvider.getCurrentPerson().orElseThrow();
        var dl = requestAttachmentService.downloadForCitizen(requestId, attachmentId, citizen);

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

    private boolean isCompletedStatus(RequestStatus status) {
        return status == RequestStatus.COMPLETED || status == RequestStatus.REJECTED;
    }
}
