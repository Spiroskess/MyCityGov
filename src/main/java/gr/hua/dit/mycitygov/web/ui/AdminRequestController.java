package gr.hua.dit.mycitygov.web.ui;

import gr.hua.dit.mycitygov.core.service.RequestService;
import gr.hua.dit.mycitygov.core.service.model.MunicipalService;
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

    public AdminRequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @GetMapping
    public String listRequests(Model model) {
        model.addAttribute("requests", requestService.getAllRequests());
        return "admin/requests";
    }

    @GetMapping("/{id}/assign")
    public String showAssignForm(@PathVariable Long id, Model model) {
        model.addAttribute("requestId", id);
        model.addAttribute("form", new AssignRequestForm());
        model.addAttribute("services", MunicipalService.values());
        return "admin/request-assign";
    }

    @PostMapping("/{id}/assign")
    public String assignToService(@PathVariable Long id,
                                  @ModelAttribute("form") @Valid AssignRequestForm form,
                                  BindingResult bindingResult,
                                  Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("requestId", id);
            model.addAttribute("services", MunicipalService.values());
            return "admin/request-assign";
        }

        requestService.assignRequestToService(id, form.getService());
        return "redirect:/admin/requests";
    }
}
