package gr.hua.dit.mycitygov.web.ui;

import gr.hua.dit.mycitygov.core.repository.RequestTypeRepository;
import gr.hua.dit.mycitygov.core.service.AppointmentService;
import gr.hua.dit.mycitygov.core.service.model.CreateAppointmentRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final RequestTypeRepository requestTypeRepository;
    private final AuthUtils authUtils;

    public AppointmentController(AppointmentService appointmentService,
                                 RequestTypeRepository requestTypeRepository,
                                 AuthUtils authUtils) {
        this.appointmentService = appointmentService;
        this.requestTypeRepository = requestTypeRepository;
        this.authUtils = authUtils;
    }

    @GetMapping
    public String listAppointments(Model model) {
        String email = authUtils.getCurrentUserEmail();
        model.addAttribute("appointments", appointmentService.getCitizenAppointments(email));
        return "appointments";
    }

    @GetMapping("/new")
    public String showNewForm(Model model) {
        model.addAttribute("createAppointment", new CreateAppointmentRequest());
        model.addAttribute("requestTypes", requestTypeRepository.findAll());
        return "appointment-form";
    }

    @PostMapping
    public String createAppointment(@Valid @ModelAttribute("createAppointment") CreateAppointmentRequest dto,
                                    BindingResult bindingResult,
                                    Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("requestTypes", requestTypeRepository.findAll());
            return "appointment-form";
        }

        String email = authUtils.getCurrentUserEmail();

        try {
            appointmentService.createAppointment(email, dto);
        } catch (IllegalStateException ex) {
            bindingResult.reject("slot.conflict", ex.getMessage());
            model.addAttribute("requestTypes", requestTypeRepository.findAll());
            return "appointment-form";
        }

        return "redirect:/appointments";
    }
}
