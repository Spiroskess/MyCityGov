package gr.hua.dit.mycitygov.web;

import gr.hua.dit.mycitygov.core.model.Person;
import gr.hua.dit.mycitygov.core.security.CurrentUserProvider;
import gr.hua.dit.mycitygov.core.service.AppointmentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Controller
@RequestMapping("/employee/appointments")
public class EmployeeAppointmentsController {

    private final AppointmentService appointmentService;
    private final CurrentUserProvider currentUserProvider;

    public EmployeeAppointmentsController(AppointmentService appointmentService,
                                          CurrentUserProvider currentUserProvider) {
        this.appointmentService = appointmentService;
        this.currentUserProvider = currentUserProvider;
    }

    private Long currentEmployeeId() {
        Person employee = currentUserProvider.getCurrentPerson().orElseThrow();
        return employee.getId();
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("appointments", appointmentService.listForEmployee(currentEmployeeId()));
        return "employee/appointments2";
    }

    @PostMapping("/{id}/confirm")
    public String confirm(@PathVariable Long id) {
        appointmentService.confirmByEmployee(currentEmployeeId(), id);
        return "redirect:/employee/appointments";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id) {
        appointmentService.cancelByEmployee(currentEmployeeId(), id);
        return "redirect:/employee/appointments";
    }

    @PostMapping("/{id}/complete")
    public String complete(@PathVariable Long id) {
        appointmentService.completeByEmployee(currentEmployeeId(), id);
        return "redirect:/employee/appointments";
    }

    @GetMapping("/{id}/reschedule")
    public String rescheduleForm(@PathVariable Long id, Model model) {
        model.addAttribute("appointmentId", id);
        return "employee/reschedule-form";
    }

    @PostMapping("/{id}/reschedule")
    public String reschedule(@PathVariable Long id,
                             @RequestParam String date,
                             @RequestParam String time) {

        LocalDate newDate = LocalDate.parse(date);
        LocalTime newTime = LocalTime.parse(time);

        appointmentService.rescheduleByEmployee(currentEmployeeId(), id, newDate, newTime);
        return "redirect:/employee/appointments";
    }
}
