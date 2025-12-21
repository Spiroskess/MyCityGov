package gr.hua.dit.mycitygov.web;

import gr.hua.dit.mycitygov.core.repository.AppointmentRepository;
import gr.hua.dit.mycitygov.core.service.model.Appointment;
import gr.hua.dit.mycitygov.core.service.model.AppointmentStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Controller
@RequestMapping("/employee/appointments")
public class EmployeeAppointmentsController {

    private final AppointmentRepository appointmentRepo;

    public EmployeeAppointmentsController(AppointmentRepository appointmentRepo) {
        this.appointmentRepo = appointmentRepo;
    }

    // =========================
    // LIST APPOINTMENTS
    // =========================
    @GetMapping
    public String list(Model model) {
        model.addAttribute("appointments", appointmentRepo.findAll());
        return "employee/appointments2";
    }

    // =========================
    // CONFIRM
    // =========================
    @PostMapping("/{id}/confirm")
    public String confirm(@PathVariable Long id) {
        Appointment a = appointmentRepo.findById(id).orElseThrow();
        a.setStatus(AppointmentStatus.CONFIRMED);
        appointmentRepo.save(a);
        return "redirect:/employee/appointments";
    }

    // =========================
    // CANCEL
    // =========================
    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id) {
        Appointment a = appointmentRepo.findById(id).orElseThrow();
        a.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepo.save(a);
        return "redirect:/employee/appointments";
    }

    // =========================
    // COMPLETE
    // =========================
    @PostMapping("/{id}/complete")
    public String complete(@PathVariable Long id) {
        Appointment a = appointmentRepo.findById(id).orElseThrow();
        a.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepo.save(a);
        return "redirect:/employee/appointments";
    }

    // =========================
    // RESCHEDULE FORM
    // =========================
    @GetMapping("/{id}/reschedule")
    public String rescheduleForm(@PathVariable Long id, Model model) {
        Appointment a = appointmentRepo.findById(id).orElseThrow();
        model.addAttribute("appointment", a);
        return "employee/reschedule-form";
    }

    // =========================
    // RESCHEDULE SUBMIT
    // =========================
    @PostMapping("/{id}/reschedule")
    public String reschedule(@PathVariable Long id,
                             @RequestParam String date,
                             @RequestParam String time) {

        Appointment a = appointmentRepo.findById(id).orElseThrow();

        LocalDate newDate = LocalDate.parse(date);
        LocalTime newTime = LocalTime.parse(time);

        a.setAppointmentDateTime(LocalDateTime.of(newDate, newTime));
        a.setStatus(AppointmentStatus.CONFIRMED);

        appointmentRepo.save(a);
        return "redirect:/employee/appointments";
    }
}
