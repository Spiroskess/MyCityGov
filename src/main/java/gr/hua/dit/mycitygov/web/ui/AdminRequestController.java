package gr.hua.dit.mycitygov.web.ui;

import gr.hua.dit.mycitygov.core.model.Person;
import gr.hua.dit.mycitygov.core.model.PersonRole;
import gr.hua.dit.mycitygov.core.repository.PersonRepository;
import gr.hua.dit.mycitygov.core.service.RequestService;
import gr.hua.dit.mycitygov.web.ui.model.AssignRequestForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/requests")
public class AdminRequestController {

    private final RequestService requestService;
    private final PersonRepository personRepository;

    public AdminRequestController(RequestService requestService,
                                  PersonRepository personRepository) {
        this.requestService = requestService;
        this.personRepository = personRepository;
    }

    @GetMapping
    public String listAllRequests(Model model) {
        model.addAttribute("requests", requestService.getAllRequests());
        return "admin/requests";
    }

    @GetMapping("/{id}/assign")
    public String showAssignForm(@PathVariable("id") Long id, Model model) {
        model.addAttribute("requestId", id);
        model.addAttribute("employees",
            personRepository.findAllByRoleOrderByLastName(PersonRole.EMPLOYEE));
        model.addAttribute("assignRequestForm", new AssignRequestForm());
        return "admin/request-assign";
    }

    @PostMapping("/{id}/assign")
    public String handleAssign(
        @PathVariable("id") Long id,
        @Valid @ModelAttribute("assignRequestForm") AssignRequestForm form,
        BindingResult bindingResult,
        Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("requestId", id);
            model.addAttribute("employees",
                personRepository.findAllByRoleOrderByLastName(PersonRole.EMPLOYEE));
            return "admin/request-assign";
        }

        Person employee = personRepository.findById(form.getEmployeeId())
            .orElseThrow();

        requestService.assignRequestToEmployee(id, employee);
        return "redirect:/admin/requests";
    }
}
