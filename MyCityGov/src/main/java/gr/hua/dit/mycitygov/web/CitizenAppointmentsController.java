package gr.hua.dit.mycitygov.web;

import gr.hua.dit.mycitygov.core.service.AppointmentService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/citizen/appointments")
public class CitizenAppointmentsController {

    private final AppointmentService appointmentService;

    public CitizenAppointmentsController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    //εμφανίζει τα ενεργά ραντεβού
    @GetMapping
    public String listActive(Authentication authentication, Model model) {
        Long citizenId = CurrentUserIds.currentUserId(authentication);
        model.addAttribute("appointments", appointmentService.listActiveForCitizen(citizenId));
        return "citizen/appointments";
    }

    //εμφανίζει τα ολοκληρωμένα/ακυρωμένα ραντεβού
    @GetMapping("/completed")
    public String listCompleted(Authentication authentication, Model model) {
        Long citizenId = CurrentUserIds.currentUserId(authentication);
        model.addAttribute("appointments", appointmentService.listCompletedForCitizen(citizenId));
        return "citizen/appointments-completed";
    }

    // Ακύρωση ραντεβού
    @PostMapping("/{id}/cancel")
    public String cancel(Authentication authentication, @PathVariable Long id) {
        Long citizenId = CurrentUserIds.currentUserId(authentication);
        appointmentService.cancelByCitizen(citizenId, id);
        return "redirect:/citizen/appointments";
    }

    // Redirect στο booking flow
    @GetMapping("/new")
    public String newAppointment() {
        return "redirect:/citizen/booking";
    }
}
