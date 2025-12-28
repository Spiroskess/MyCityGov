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

    //Ενεργά ραντεβού menu:"Ραντεβού"
    @GetMapping
    public String listActive(Authentication authentication, Model model) {
        Long citizenId = CurrentUserIds.currentUserId(authentication);
        model.addAttribute("appointments", appointmentService.listActiveForCitizen(citizenId));
        return "citizen/appointments";
    }

    //Ολοκληρωμένα/Ακυρωμένα
    @GetMapping("/completed")
    public String listCompleted(Authentication authentication, Model model) {
        Long citizenId = CurrentUserIds.currentUserId(authentication);
        model.addAttribute("appointments", appointmentService.listCompletedForCitizen(citizenId));
        return "citizen/appointments-completed";
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
