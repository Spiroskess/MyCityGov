package gr.hua.dit.mycitygov.web.ui;

import gr.hua.dit.mycitygov.core.model.RequestStatus;
import gr.hua.dit.mycitygov.core.service.EmployeeRequestService;
import gr.hua.dit.mycitygov.core.service.model.UpdateRequestStatusRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/employee/requests")
public class EmployeeRequestController {

    private final EmployeeRequestService employeeRequestService;
    private final AuthUtils authUtils;

    public EmployeeRequestController(EmployeeRequestService employeeRequestService,
                                     AuthUtils authUtils) {
        this.employeeRequestService = employeeRequestService;
        this.authUtils = authUtils;
    }

    @GetMapping
    public String listAllRequests(Model model) {
        model.addAttribute("requests", employeeRequestService.getAllRequests());
        return "employee-requests";
    }

    @GetMapping("/assigned")
    public String listAssignedRequests(Model model) {
        String email = authUtils.getCurrentUserEmail();
        model.addAttribute("requests", employeeRequestService.getAssignedRequests(email));
        return "employee-requests";
    }

    @GetMapping("/{id}")
    public String showRequest(@PathVariable Long id, Model model) {
        // Πολύ απλά: ξαναχρησιμοποιούμε τη λίστα και βρίσκουμε ένα
        model.addAttribute("requestId", id);
        model.addAttribute("statusValues", RequestStatus.values());
        model.addAttribute("updateRequest", new UpdateRequestStatusRequest());
        return "employee-request-details";
    }

    @PostMapping("/{id}/assign")
    public String assignToSelf(@PathVariable Long id) {
        String email = authUtils.getCurrentUserEmail();
        employeeRequestService.assignToSelf(id, email);
        return "redirect:/employee/requests";
    }

    @PostMapping("/{id}/update")
    public String updateStatus(@PathVariable Long id,
                               @Valid @ModelAttribute("updateRequest") UpdateRequestStatusRequest dto,
                               BindingResult bindingResult,
                               Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("requestId", id);
            model.addAttribute("statusValues", RequestStatus.values());
            return "employee-request-details";
        }

        String email = authUtils.getCurrentUserEmail();
        employeeRequestService.updateStatus(id, email, dto);
        return "redirect:/employee/requests";
    }
}
