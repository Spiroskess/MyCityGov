package gr.hua.dit.mycitygov.web.ui;

import gr.hua.dit.mycitygov.core.model.Person;
import gr.hua.dit.mycitygov.core.model.RequestStatus;
import gr.hua.dit.mycitygov.core.security.CurrentUserProvider;
import gr.hua.dit.mycitygov.core.service.RequestService;
import gr.hua.dit.mycitygov.core.service.model.MunicipalService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class EmployeeRequestController {

    private final RequestService requestService;
    private final CurrentUserProvider currentUserProvider;

    public EmployeeRequestController(RequestService requestService, CurrentUserProvider currentUserProvider) {
        this.requestService = requestService;
        this.currentUserProvider = currentUserProvider;
    }

    /**
     * Το base.html δείχνει στο /employee/requests.
     * Αυτό λειτουργεί σαν "index" και σε στέλνει στη λίστα αιτημάτων υπηρεσίας.
     */
    @GetMapping("/employee/requests")
    public String employeeRequestsIndex() {
        return "redirect:/employee/requests-service";
    }

    /**
     * Αιτήματα υπηρεσίας
     * Template: employee/requests-service.html
     */
    @GetMapping("/employee/requests-service")
    public String serviceQueue(Model model) {
        Person employee = currentUserProvider.getCurrentPerson().orElseThrow();
        MunicipalService service = employee.getMunicipalService();

        model.addAttribute("service", service);
        model.addAttribute("requests", requestService.getServiceQueue(service));
        return "employee/requests-service";
    }

    /**
     * Αιτήματα που έχει αναλάβει ο υπάλληλος
     * Template: employee/requests-mine.html
     */
    @GetMapping("/employee/requests-mine")
    public String myRequests(Model model) {
        Person employee = currentUserProvider.getCurrentPerson().orElseThrow();
        model.addAttribute("requests", requestService.getRequestsAssignedToEmployee(employee));
        return "employee/requests-mine";
    }

    /**
     * Ανάληψη αιτήματος
     * POST από τη σελίδα requests-service
     */
    @PostMapping("/employee/requests/claim")
    public String claim(@RequestParam Long requestId) {
        Person employee = currentUserProvider.getCurrentPerson().orElseThrow();
        requestService.claimRequest(requestId, employee);
        return "redirect:/employee/requests-service";
    }

    /**
     * Αλλαγή status + comment
     * POST από τη σελίδα requests-mine
     */
    @PostMapping("/employee/requests/status")
    public String updateStatus(@RequestParam Long requestId,
                               @RequestParam RequestStatus nextStatus,
                               @RequestParam(required = false) String comment) {

        Person employee = currentUserProvider.getCurrentPerson().orElseThrow();
        requestService.updateStatus(requestId, employee, nextStatus, comment);

        return "redirect:/employee/requests-mine";
    }
}
