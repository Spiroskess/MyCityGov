package gr.hua.dit.mycitygov.web.ui;

import gr.hua.dit.mycitygov.core.model.Person;
import gr.hua.dit.mycitygov.core.model.RequestStatus;
import gr.hua.dit.mycitygov.core.security.CurrentUserProvider;
import gr.hua.dit.mycitygov.core.service.RequestAttachmentService;
import gr.hua.dit.mycitygov.core.service.RequestService;
import gr.hua.dit.mycitygov.core.service.RequestTypeService;
import gr.hua.dit.mycitygov.core.service.model.AttachmentUpload;
import gr.hua.dit.mycitygov.core.service.model.OpenRequestRequest;
import gr.hua.dit.mycitygov.core.service.model.RequestView;

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

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CitizenRequestController {

    private final RequestService requestService;
    private final RequestTypeService requestTypeService;
    private final CurrentUserProvider currentUserProvider;
    private final RequestAttachmentService requestAttachmentService;

    public CitizenRequestController(RequestService requestService,
                                    RequestTypeService requestTypeService,
                                    CurrentUserProvider currentUserProvider,
                                    RequestAttachmentService requestAttachmentService) {
        this.requestService = requestService;
        this.requestTypeService = requestTypeService;
        this.currentUserProvider = currentUserProvider;
        this.requestAttachmentService = requestAttachmentService;
    }

    @GetMapping("/citizen/requests")
    public String listCitizenRequests(Model model) {
        //  εμφανίζει τα ενεργά αιτήματα του πολίτη
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
        // εμφανίζει ιστορικό ολοκληρωμένων/απορριφθέντων αιτημάτων
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
        // Φόρμα δημιουργίας νέου αιτήματος
        model.addAttribute("openRequestRequest", new OpenRequestRequest("", "", ""));
        model.addAttribute("requestTypes", requestTypeService.listEnabled());
        return "citizen/request-new";
    }

    @PostMapping("/citizen/request-new")
    public String handleNewRequest(
        @Valid @ModelAttribute("openRequestRequest") OpenRequestRequest openRequestRequest,
        BindingResult bindingResult,
        @RequestParam(name = "attachments", required = false) MultipartFile[] attachments,
        Model model) {

        // Submit νέου αιτήματος + (optional) upload συνημμένων σε S3/MinIO
        if (bindingResult.hasErrors()) {
            model.addAttribute("requestTypes", requestTypeService.listEnabled());
            return "citizen/request-new";
        }

        Person citizen = currentUserProvider.getCurrentPerson().orElseThrow();

        RequestView created;
        try {
            created = requestService.openRequest(citizen, openRequestRequest);
        } catch (IllegalArgumentException ex) {
            // π.χ. UNKNOWN_REQUEST_TYPE / REQUEST_TYPE_DISABLED
            bindingResult.rejectValue("requestTypeCode", "invalid", "Μη έγκυρος τύπος αιτήματος.");
            model.addAttribute("requestTypes", requestTypeService.listEnabled());
            return "citizen/request-new";
        }

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
                    model.addAttribute("requestTypes", requestTypeService.listEnabled());
                    return "citizen/request-new";
                }
            }
        }

        return "redirect:/citizen/requests";
    }

    @GetMapping("/citizen/requests/{id}")
    public String citizenRequestDetails(@PathVariable Long id, Model model) {
        // detail σελίδα αιτήματος (μαζί με messages + attachments)
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
    @PostMapping("/citizen/requests/{id}/attachments/upload")
    public String uploadAdditionalAttachments(@PathVariable Long id,
                                              @RequestParam(name = "attachments", required = false) MultipartFile[] attachments,
                                              @RequestParam(name = "note", required = false) String note,
                                              RedirectAttributes ra) {

        Person citizen = currentUserProvider.getCurrentPerson().orElseThrow();

        int uploaded = 0;
        List<String> failed = new ArrayList<>();

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
                    // IMPORTANT: αυτό είναι το σωστό για WAITING_ADDITIONAL_INFO
                    requestAttachmentService.addAdditionalInfoForCitizenRequest(id, citizen, upload);
                    uploaded++;
                } catch (Exception e) {
                    failed.add(f.getOriginalFilename() == null ? "file" : f.getOriginalFilename());
                }
            }
        }

        // Αν δεν έστειλε ούτε αρχείο ούτε σημείωση
        if (uploaded == 0 && (note == null || note.trim().isEmpty())) {
            ra.addFlashAttribute("uploadError", "Δεν επέλεξες αρχεία και δεν έγραψες σημείωση.");
            return "redirect:/citizen/requests/" + id;
        }

        // Αν απέτυχαν κάποια αρχεία
        if (!failed.isEmpty()) {
            ra.addFlashAttribute("uploadError", "Αποτυχία ανεβάσματος: " + String.join(", ", failed));
            return "redirect:/citizen/requests/" + id;
        }

        // Γράψε μήνυμα στο ιστορικό (υπάρχει ήδη στο RequestServiceImpl)
        requestService.citizenSubmittedAdditionalInfo(id, citizen, uploaded, note);

        if (uploaded > 0) {
            ra.addFlashAttribute("uploadOk", "Ανέβηκαν " + uploaded + " αρχείο(α) επιτυχώς.");
        } else {
            ra.addFlashAttribute("uploadOk", "Το συμπληρωματικό μήνυμα καταχωρήθηκε.");
        }

        return "redirect:/citizen/requests/" + id;
    }

    @GetMapping("/citizen/requests/{requestId}/attachments/{attachmentId}/download")
    @ResponseBody
    public ResponseEntity<Resource> downloadCitizenAttachment(@PathVariable Long requestId,
                                                              @PathVariable Long attachmentId) {
        // Download συνημμένου από πολίτη
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
        // Τερματικές καταστάσεις για “Completed” tab
        return status == RequestStatus.COMPLETED || status == RequestStatus.REJECTED;
    }
}
