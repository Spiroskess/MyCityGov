package gr.hua.dit.mycitygov.web;

import gr.hua.dit.mycitygov.core.service.AppointmentService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/citizen/appointments")
public class CitizenAppointmentsController {

    private final AppointmentService appointmentService;

    public CitizenAppointmentsController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping
    public String list(Authentication authentication, Model model) {
        Long citizenId = CurrentUserIds.currentUserId(authentication);
        model.addAttribute("appointments", appointmentService.listForCitizen(citizenId));
        return "citizen/appointments";
    }

    // Ακύρωση
    @PostMapping("/{id}/cancel")
    public String cancel(Authentication authentication, @PathVariable Long id) {
        Long citizenId = CurrentUserIds.currentUserId(authentication);
        appointmentService.cancelByCitizen(citizenId, id);
        return "redirect:/citizen/appointments";
    }

    // Νέο ραντεβού - booking flow
    @GetMapping("/new")
    public String newAppointment() {
        return "redirect:/citizen/booking";
    }
}
