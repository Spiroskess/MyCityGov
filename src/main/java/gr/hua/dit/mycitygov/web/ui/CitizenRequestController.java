package gr.hua.dit.mycitygov.web.ui;

import gr.hua.dit.mycitygov.core.model.Person;
import gr.hua.dit.mycitygov.core.model.RequestStatus;
import gr.hua.dit.mycitygov.core.security.CurrentUserProvider;
import gr.hua.dit.mycitygov.core.service.RequestService;
import gr.hua.dit.mycitygov.core.service.model.OpenRequestRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class CitizenRequestController {

    private final RequestService requestService;
    private final CurrentUserProvider currentUserProvider;

    public CitizenRequestController(RequestService requestService,
                                    CurrentUserProvider currentUserProvider) {
        this.requestService = requestService;
        this.currentUserProvider = currentUserProvider;
    }

    // ΕΝΕΡΓΑ αιτήματα
    @GetMapping("/citizen/requests")
    public String listCitizenRequests(Model model) {
        Person citizen = currentUserProvider.getCurrentPerson()
            .orElseThrow();

        var all = requestService.getRequestsOfCitizen(citizen);
        var active = all.stream()
            .filter(r -> !isCompletedStatus(r.status()))
            .toList();

        model.addAttribute("requests", active);
        return "citizen/requests";
    }

    // ΟΛΟΚΛΗΡΩΜΕΝΑ αιτήματα
    @GetMapping("/citizen/requests/completed")
    public String listCitizenCompletedRequests(Model model) {
        Person citizen = currentUserProvider.getCurrentPerson()
            .orElseThrow();

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
        Model model) {

        if (bindingResult.hasErrors()) {
            return "citizen/request-new";
        }

        Person citizen = currentUserProvider.getCurrentPerson()
            .orElseThrow();

        requestService.openRequest(citizen, openRequestRequest);
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
        return "citizen/request-details";
    }

    // "Ολοκληρωμένα" θεωρούμε: COMPLETED ή REJECTED
    private boolean isCompletedStatus(RequestStatus status) {
        return status == RequestStatus.COMPLETED || status == RequestStatus.REJECTED;
    }
}
