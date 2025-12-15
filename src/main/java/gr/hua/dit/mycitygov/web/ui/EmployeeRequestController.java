package gr.hua.dit.mycitygov.web.ui;

import gr.hua.dit.mycitygov.core.model.Person;
import gr.hua.dit.mycitygov.core.model.RequestStatus;
import gr.hua.dit.mycitygov.core.security.CurrentUserProvider;
import gr.hua.dit.mycitygov.core.service.RequestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class EmployeeRequestController {

    private final RequestService requestService;
    private final CurrentUserProvider currentUserProvider;

    public EmployeeRequestController(RequestService requestService,
                                     CurrentUserProvider currentUserProvider) {
        this.requestService = requestService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/employee/requests")
    public String listAssignedRequests(Model model) {
        Person employee = currentUserProvider.getCurrentPerson().orElseThrow();
        model.addAttribute("requests", requestService.getRequestsAssignedToEmployee(employee));
        return "employee/requests";
    }

    @PostMapping("/employee/requests/status")
    public String updateStatus(@RequestParam Long requestId,
                               @RequestParam RequestStatus nextStatus,
                               @RequestParam(required = false) String comment) {

        Person employee = currentUserProvider.getCurrentPerson().orElseThrow();
        requestService.updateStatus(requestId, employee, nextStatus, comment);

        return "redirect:/employee/requests";
    }
}
