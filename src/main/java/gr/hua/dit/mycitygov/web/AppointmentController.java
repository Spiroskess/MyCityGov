package gr.hua.dit.mycitygov.web;

import gr.hua.dit.mycitygov.core.repository.AppointmentRepository;
import gr.hua.dit.mycitygov.core.service.model.Appointment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class AppointmentController {

    private final AppointmentRepository appointmentRepository;

    public AppointmentController(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    // Προσωρινά μέχρι να το δέσουμε με logged-in user
    private Long currentCitizenId() {
        return 1L;
    }

    // ✅ Λίστα ραντεβού για πολίτη (μόνο τα δικά του)
    @GetMapping("/citizen/appointments")
    public String listAppointments(Model model) {
        Long citizenId = currentCitizenId();
        List<Appointment> appointments = appointmentRepository.findByCitizenId(citizenId);
        model.addAttribute("appointments", appointments);
        return "citizen/appointments";
    }

    // ✅ Το "Νέο ραντεβού" πάει στο νέο booking flow
    @GetMapping("/citizen/appointments/new")
    public String newAppointment() {
        return "redirect:/citizen/booking";
    }
}
