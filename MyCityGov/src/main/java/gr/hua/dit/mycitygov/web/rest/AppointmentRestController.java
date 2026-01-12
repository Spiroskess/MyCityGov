package gr.hua.dit.mycitygov.web.rest;

import gr.hua.dit.mycitygov.core.model.Appointment;
import gr.hua.dit.mycitygov.core.model.Person;
import gr.hua.dit.mycitygov.core.repository.PersonRepository;
import gr.hua.dit.mycitygov.core.service.AppointmentService;
import gr.hua.dit.mycitygov.web.rest.model.BookAppointmentRequest;
import gr.hua.dit.mycitygov.web.rest.model.RescheduleAppointmentRequest;
import gr.hua.dit.mycitygov.web.rest.model.UpdateAppointmentStatusRequest;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping(value = "/api/appointments", produces = MediaType.APPLICATION_JSON_VALUE)
public class AppointmentRestController {

    private final AppointmentService appointmentService;
    private final PersonRepository personRepository;

    public AppointmentRestController(AppointmentService appointmentService, PersonRepository personRepository) {
        this.appointmentService = appointmentService;
        this.personRepository = personRepository;
    }

    // -------------------------
    // Helpers
    // -------------------------
    private Person me(Authentication auth) {
        String email = auth.getName();
        return personRepository.findByEmailAddressIgnoreCase(email)
            .orElseThrow(() -> new IllegalStateException("User not found by email: " + email));
    }

    // -------------------------
    // ADMIN
    // -------------------------

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<Appointment> getAllAppointments() {
        return appointmentService.listForAdmin();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping(value = "/{appointmentId}/status", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Appointment setStatusByAdmin(
        @PathVariable Long appointmentId,
        @Valid @RequestBody UpdateAppointmentStatusRequest request
    ) {
        return appointmentService.setStatusByAdmin(appointmentId, request.status);
    }

    // -------------------------
    // CITIZEN
    // -------------------------

    @PreAuthorize("hasRole('CITIZEN')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Appointment book(@Valid @RequestBody BookAppointmentRequest request, Authentication auth) {
        Person me = me(auth);
        return appointmentService.book(me.getId(), request.service, request.date, request.time);
    }

    @PreAuthorize("hasRole('CITIZEN')")
    @GetMapping("/my")
    public List<Appointment> myAppointments(Authentication auth) {
        Person me = me(auth);
        return appointmentService.listForCitizen(me.getId());
    }

    @PreAuthorize("hasRole('CITIZEN')")
    @GetMapping("/my/active")
    public List<Appointment> myActive(Authentication auth) {
        Person me = me(auth);
        return appointmentService.listActiveForCitizen(me.getId());
    }

    @PreAuthorize("hasRole('CITIZEN')")
    @GetMapping("/my/completed")
    public List<Appointment> myCompleted(Authentication auth) {
        Person me = me(auth);
        return appointmentService.listCompletedForCitizen(me.getId());
    }

    // Σημ: το "DELETE" εδώ στην πράξη = cancel (όπως το βλέπεις στα responses σου)
    @PreAuthorize("hasRole('CITIZEN')")
    @DeleteMapping("/{appointmentId}")
    public Appointment cancelMyAppointment(@PathVariable Long appointmentId, Authentication auth) {
        Person me = me(auth);
        return appointmentService.cancelByCitizen(me.getId(), appointmentId);
    }

    // -------------------------
    // EMPLOYEE
    // -------------------------

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/employee")
    public List<Appointment> listForEmployee(Authentication auth) {
        Person me = me(auth);
        return appointmentService.listForEmployee(me.getId());
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PatchMapping("/{appointmentId}/confirm")
    public Appointment confirm(@PathVariable Long appointmentId, Authentication auth) {
        Person me = me(auth);
        return appointmentService.confirmByEmployee(me.getId(), appointmentId);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PatchMapping("/{appointmentId}/complete")
    public Appointment complete(@PathVariable Long appointmentId, Authentication auth) {
        Person me = me(auth);
        return appointmentService.completeByEmployee(me.getId(), appointmentId);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PatchMapping(value = "/{appointmentId}/reschedule", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Appointment reschedule(
        @PathVariable Long appointmentId,
        @Valid @RequestBody RescheduleAppointmentRequest request,
        Authentication auth
    ) {
        Person me = me(auth);
        return appointmentService.rescheduleByEmployee(me.getId(), appointmentId, request.date, request.time);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PatchMapping("/{appointmentId}/cancel")
    public Appointment cancelByEmployee(@PathVariable Long appointmentId, Authentication auth) {
        Person me = me(auth);
        return appointmentService.cancelByEmployee(me.getId(), appointmentId);
    }
}
