package gr.hua.dit.mycitygov.web.ui;

import gr.hua.dit.mycitygov.core.service.RequestService;
import gr.hua.dit.mycitygov.core.service.model.MunicipalService;
import gr.hua.dit.mycitygov.core.service.model.RequestView;
import gr.hua.dit.mycitygov.web.ui.model.AssignRequestForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/requests")
public class AdminRequestController {

    private final RequestService requestService;

    public AdminRequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @GetMapping
    public String listRequests(
        @RequestParam(name = "view", defaultValue = "all") String view,
        Model model
    ) {
        //  λίστα αιτημάτων με φίλτρο
        String safeView = normalizeView(view);

        List<RequestView> all = requestService.getAllRequests();
        List<RequestView> unassigned = requestService.getUnassignedRequests();
        List<RequestView> assigned = requestService.getAssignedRequests();

        List<RequestView> shown = switch (safeView) {
            case "unassigned" -> unassigned;
            case "assigned" -> assigned;
            default -> all;
        };

        model.addAttribute("view", safeView);
        model.addAttribute("requests", shown);

        // counts για tabs/filters στο template
        model.addAttribute("countAll", all.size());
        model.addAttribute("countUnassigned", unassigned.size());
        model.addAttribute("countAssigned", assigned.size());

        return "admin/requests";
    }

    @GetMapping("/{id}/assign")
    public String showAssignForm(
        @PathVariable Long id,
        @RequestParam(name = "view", defaultValue = "all") String view,
        Model model
    ) {
        // φόρμα ανάθεσης αιτήματος σε δημοτική υπηρεσία
        model.addAttribute("requestId", id);
        model.addAttribute("services", MunicipalService.values());
        model.addAttribute("form", new AssignRequestForm());

        model.addAttribute("view", normalizeView(view)); // επιστροφή στην ίδια προβολή
        return "admin/request-assign";
    }

    @PostMapping("/{id}/assign")
    public String assignToService(
        @PathVariable Long id,
        @RequestParam(name = "view", defaultValue = "all") String view,
        @ModelAttribute("form") @Valid AssignRequestForm form,
        BindingResult bindingResult,
        Model model
    ) {
        // POST ανάθεσης με validation (π.χ. να έχει επιλεγεί υπηρεσία)
        String safeView = normalizeView(view);

        if (bindingResult.hasErrors()) {
            model.addAttribute("requestId", id);
            model.addAttribute("services", MunicipalService.values());
            model.addAttribute("view", safeView);
            return "admin/request-assign";
        }

        requestService.assignRequestToService(id, form.getService());

        return "redirect:/admin/requests?view=" + safeView;
    }

    private String normalizeView(String view) {
        // Sanitization για να μην περνάνε άκυρες τιμές query param στο UI
        if (view == null) return "all";
        return switch (view) {
            case "all", "unassigned", "assigned" -> view;
            default -> "all";
        };
    }
}
