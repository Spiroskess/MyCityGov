package gr.hua.dit.mycitygov.web;

import gr.hua.dit.mycitygov.core.model.Appointment;
import gr.hua.dit.mycitygov.core.model.Person;
import gr.hua.dit.mycitygov.core.repository.AppointmentRepository;
import gr.hua.dit.mycitygov.core.security.CurrentUserProvider;
import gr.hua.dit.mycitygov.core.service.AppointmentService;
import gr.hua.dit.mycitygov.core.service.AvailabilityService;
import gr.hua.dit.mycitygov.core.service.model.MunicipalService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Controller
@RequestMapping("/employee/appointments")
public class EmployeeAppointmentsController {

    private final AppointmentService appointmentService;
    private final AvailabilityService availabilityService;

    // Repository χρησιμοποιείται εδώ μόνο για “load + access check” του appointment (όχι business logic)
    private final AppointmentRepository appointmentRepository;

    private final CurrentUserProvider currentUserProvider;

    public EmployeeAppointmentsController(AppointmentService appointmentService,
                                          AvailabilityService availabilityService,
                                          AppointmentRepository appointmentRepository,
                                          CurrentUserProvider currentUserProvider) {
        this.appointmentService = appointmentService;
        this.availabilityService = availabilityService;
        this.appointmentRepository = appointmentRepository;
        this.currentUserProvider = currentUserProvider;
    }

    // Helper: παίρνει το current employee (Person) από το session/security context
    private Person currentEmployee() {
        return currentUserProvider.getCurrentPerson().orElseThrow();
    }

    private Long currentEmployeeId() {
        return currentEmployee().getId();
    }

    // Φόρτωση ραντεβού με έλεγχο ότι ο υπάλληλος έχει δικαίωμα
    private Appointment loadAppointmentForEmployee(Long employeeId, Long appointmentId) {
        Appointment a = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Το ραντεβού δεν βρέθηκε."));

        Person employee = currentEmployee();
        MunicipalService svc = employee.getMunicipalService();

        // Αν ο υπάλληλος έχει υπηρεσία, βλέπει μόνο ραντεβού της υπηρεσίας του
        if (svc != null) {
            if (a.getService() != svc) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Δεν έχεις πρόσβαση σε ραντεβού άλλης υπηρεσίας.");
            }
        } else {
            // Αν δεν έχει υπηρεσία, βλέπει μόνο όσα του έχουν ανατεθεί
            if (a.getEmployeeId() == null || !a.getEmployeeId().equals(employeeId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Δεν έχεις πρόσβαση σε αυτό το ραντεβού.");
            }
        }

        return a;
    }

    // Μεταφράζει “τεχνικά” service error messages σε φιλικά UI μηνύματα
    private String mapRescheduleError(String msg) {
        if (msg == null || msg.isBlank()) return "Αποτυχία αλλαγής ώρας.";
        return switch (msg) {
            case "Time slot not available" -> "Η επιλεγμένη ώρα δεν είναι διαθέσιμη.";
            case "Service overlap" -> "Υπάρχει ήδη ραντεβού της υπηρεσίας στην ίδια ώρα.";
            case "Employee overlap" -> "Έχεις ήδη άλλο ραντεβού την ίδια ώρα.";
            case "Not your appointment" -> "Το ραντεβού είναι ανατεθειμένο σε άλλον υπάλληλο.";
            case "Cannot reschedule cancelled appointment" -> "Δεν μπορείς να αλλάξεις ώρα σε ακυρωμένο ραντεβού.";
            case "Cannot reschedule completed appointment" -> "Δεν μπορείς να αλλάξεις ώρα σε ολοκληρωμένο ραντεβού.";
            default -> msg;
        };
    }

    @GetMapping
    public String list(Model model) {
        // Employee UI: λίστα ραντεβού που μπορεί να διαχειριστεί ο υπάλληλος
        model.addAttribute("appointments", appointmentService.listForEmployee(currentEmployeeId()));
        return "employee/appointments2";
    }

    @PostMapping("/{id}/confirm")
    public String confirm(@PathVariable Long id) {
        // Επιβεβαίωση ραντεβού
        appointmentService.confirmByEmployee(currentEmployeeId(), id);
        return "redirect:/employee/appointments";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id) {
        // Ακύρωση ραντεβού από υπάλληλο
        appointmentService.cancelByEmployee(currentEmployeeId(), id);
        return "redirect:/employee/appointments";
    }

    @PostMapping("/{id}/complete")
    public String complete(@PathVariable Long id) {
        // Ολοκλήρωση ραντεβού από υπάλληλο
        appointmentService.completeByEmployee(currentEmployeeId(), id);
        return "redirect:/employee/appointments";
    }

    @GetMapping("/{id}/reschedule")
    public String rescheduleForm(@PathVariable Long id,
                                 @RequestParam(required = false) String date,
                                 Model model) {
        // UI φόρμα αλλαγής ώρας: δείχνει διαθέσιμα slots για την επιλεγμένη μέρα
        Long employeeId = currentEmployeeId();
        Appointment appointment = loadAppointmentForEmployee(employeeId, id);

        LocalDate selectedDate;
        if (date != null && !date.isBlank()) {
            selectedDate = LocalDate.parse(date);
        } else if (appointment.getAppointmentDateTime() != null) {
            selectedDate = appointment.getAppointmentDateTime().toLocalDate();
        } else {
            selectedDate = LocalDate.now();
        }

        List<LocalTime> availableTimes =
            availabilityService.getAvailableTimes(appointment.getService(), selectedDate, appointment.getId());

        LocalTime selectedTime = null;
        if (appointment.getAppointmentDateTime() != null
            && selectedDate.equals(appointment.getAppointmentDateTime().toLocalDate())) {
            selectedTime = appointment.getAppointmentDateTime().toLocalTime();
        }

        model.addAttribute("appointment", appointment);
        model.addAttribute("selectedDate", selectedDate);
        model.addAttribute("availableTimes", availableTimes);
        model.addAttribute("selectedTime", selectedTime);

        return "employee/reschedule-form";
    }

    @PostMapping("/{id}/reschedule")
    public String reschedule(@PathVariable Long id,
                             @RequestParam String date,
                             @RequestParam String time,
                             Model model) {
        // Submit αλλαγής ώρας: αν αποτύχει, ξαναδείχνει τη φόρμα με error
        Long employeeId = currentEmployeeId();
        Appointment appointment = loadAppointmentForEmployee(employeeId, id);

        LocalDate newDate = LocalDate.parse(date);
        LocalTime newTime = LocalTime.parse(time);

        try {
            appointmentService.rescheduleByEmployee(employeeId, id, newDate, newTime);
            return "redirect:/employee/appointments";
        } catch (Exception ex) {
            model.addAttribute("appointment", appointment);
            model.addAttribute("selectedDate", newDate);
            model.addAttribute(
                "availableTimes",
                availabilityService.getAvailableTimes(appointment.getService(), newDate, appointment.getId())
            );
            model.addAttribute("selectedTime", newTime);
            model.addAttribute("error", mapRescheduleError(ex.getMessage()));
            return "employee/reschedule-form";
        }
    }
}
