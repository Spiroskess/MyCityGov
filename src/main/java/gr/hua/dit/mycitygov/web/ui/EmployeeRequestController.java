package gr.hua.dit.mycitygov.web.ui;

import gr.hua.dit.mycitygov.core.model.Person;
import gr.hua.dit.mycitygov.core.model.RequestStatus;
import gr.hua.dit.mycitygov.core.security.CurrentUserProvider;
import gr.hua.dit.mycitygov.core.service.RequestService;
import gr.hua.dit.mycitygov.core.service.model.MunicipalService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class EmployeeRequestController {

    private final RequestService requestService;
    private final CurrentUserProvider currentUserProvider;

    public EmployeeRequestController(RequestService requestService, CurrentUserProvider currentUserProvider) {
        this.requestService = requestService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/employee/requests")
    public String employeeRequestsIndex() {
        return "redirect:/employee/requests-service";
    }

    @GetMapping("/employee/requests-service")
    public String serviceQueue(Model model) {
        Person employee = currentUserProvider.getCurrentPerson().orElseThrow();
        MunicipalService service = employee.getMunicipalService();

        model.addAttribute("service", service);
        model.addAttribute("requests", requestService.getServiceQueue(service));
        return "employee/requests-service";
    }

    @GetMapping("/employee/requests-mine")
    public String myRequests(Model model) {
        Person employee = currentUserProvider.getCurrentPerson().orElseThrow();
        model.addAttribute("requests", requestService.getRequestsAssignedToEmployee(employee));
        return "employee/requests-mine";
    }

    @GetMapping("/employee/requests/{id}")
    public String requestDetails(@PathVariable Long id,
                                 @RequestParam(required = false) String err,
                                 Model model) {
        Person employee = currentUserProvider.getCurrentPerson().orElseThrow();

        var opt = requestService.getMyRequestDetails(id, employee);
        if (opt.isEmpty()) {
            return "redirect:/employee/requests-mine";
        }

        model.addAttribute("r", opt.get());
        model.addAttribute("messages", requestService.getMyRequestMessages(id, employee)); // ✅
        model.addAttribute("err", err);

        return "employee/request-details";
    }

    @PostMapping("/employee/requests/claim")
    public String claim(@RequestParam Long requestId) {
        Person employee = currentUserProvider.getCurrentPerson().orElseThrow();
        requestService.claimRequest(requestId, employee);
        return "redirect:/employee/requests-service";
    }

    @PostMapping("/employee/requests/status")
    public String updateStatus(@RequestParam Long requestId,
                               @RequestParam RequestStatus nextStatus,
                               @RequestParam(required = false) String comment,
                               @RequestParam(required = false) String redirectTo) {

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
