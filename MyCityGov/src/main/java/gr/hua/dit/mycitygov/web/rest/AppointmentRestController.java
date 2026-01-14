package gr.hua.dit.mycitygov.web.rest;

import gr.hua.dit.mycitygov.core.model.Appointment;
import gr.hua.dit.mycitygov.core.security.CurrentUserProvider;
import gr.hua.dit.mycitygov.core.service.AppointmentService;
import gr.hua.dit.mycitygov.web.rest.model.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "BearerAuth")
@Tag(name = "1 - Appointments")
@RestController
@RequestMapping(value = "/api/appointments", produces = MediaType.APPLICATION_JSON_VALUE)
public class AppointmentRestController {

    private final AppointmentService appointmentService;
    private final CurrentUserProvider currentUserProvider;

    public AppointmentRestController(AppointmentService appointmentService, CurrentUserProvider currentUserProvider) {
        this.appointmentService = appointmentService;
        this.currentUserProvider = currentUserProvider;
    }

    private AppointmentDto toDto(Appointment a) {
        return new AppointmentDto(
            a.getId(),
            a.getCitizenId(),
            a.getEmployeeId(),
            a.getService(),
            a.getAppointmentDateTime(),
            a.getStatus()
        );
    }

    // -------------------------
    // ADMIN
    // -------------------------

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<AppointmentDto> getAllAppointments() {
        return appointmentService.listForAdmin().stream().map(this::toDto).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping(value = "/{appointmentId}/status", consumes = MediaType.APPLICATION_JSON_VALUE)
    public AppointmentDto setStatusByAdmin(
        @PathVariable Long appointmentId,
        @Valid @RequestBody UpdateAppointmentStatusRequest request
    ) {
        return toDto(appointmentService.setStatusByAdmin(appointmentId, request.status));
    }

    // -------------------------
    // CITIZEN
    // -------------------------

    @PreAuthorize("hasRole('CITIZEN')")
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public AppointmentDto book(@Valid @RequestBody BookAppointmentRequest request) {
        long citizenId = currentUserProvider.requireCitizenId();
        return toDto(appointmentService.book(citizenId, request.service, request.date, request.time));
    }

    @PreAuthorize("hasRole('CITIZEN')")
    @GetMapping("/my")
    public List<AppointmentDto> myAppointments() {
        long citizenId = currentUserProvider.requireCitizenId();
        return appointmentService.listForCitizen(citizenId).stream().map(this::toDto).toList();
    }

    @PreAuthorize("hasRole('CITIZEN')")
    @GetMapping("/my/active")
    public List<AppointmentDto> myActive() {
        long citizenId = currentUserProvider.requireCitizenId();
        return appointmentService.listActiveForCitizen(citizenId).stream().map(this::toDto).toList();
    }

    @PreAuthorize("hasRole('CITIZEN')")
    @GetMapping("/my/completed")
    public List<AppointmentDto> myCompleted() {
        long citizenId = currentUserProvider.requireCitizenId();
        return appointmentService.listCompletedForCitizen(citizenId).stream().map(this::toDto).toList();
    }

    @PreAuthorize("hasRole('CITIZEN')")
    @DeleteMapping("/{appointmentId}")
    public AppointmentDto cancelMyAppointment(@PathVariable Long appointmentId) {
        long citizenId = currentUserProvider.requireCitizenId();
        return toDto(appointmentService.cancelByCitizen(citizenId, appointmentId));
    }

    // -------------------------
    // EMPLOYEE
    // -------------------------

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/employee")
    public List<AppointmentDto> listForEmployee() {
        long employeeId = currentUserProvider.requireEmployeeId();
        return appointmentService.listForEmployee(employeeId).stream().map(this::toDto).toList();
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PatchMapping("/{appointmentId}/confirm")
    public AppointmentDto confirm(@PathVariable Long appointmentId) {
        long employeeId = currentUserProvider.requireEmployeeId();
        return toDto(appointmentService.confirmByEmployee(employeeId, appointmentId));
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PatchMapping("/{appointmentId}/complete")
    public AppointmentDto complete(@PathVariable Long appointmentId) {
        long employeeId = currentUserProvider.requireEmployeeId();
        return toDto(appointmentService.completeByEmployee(employeeId, appointmentId));
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PatchMapping(value = "/{appointmentId}/reschedule", consumes = MediaType.APPLICATION_JSON_VALUE)
    public AppointmentDto reschedule(
        @PathVariable Long appointmentId,
        @Valid @RequestBody RescheduleAppointmentRequest request
    ) {
        long employeeId = currentUserProvider.requireEmployeeId();
        return toDto(appointmentService.rescheduleByEmployee(employeeId, appointmentId, request.date, request.time));
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PatchMapping("/{appointmentId}/cancel")
    public AppointmentDto cancelByEmployee(@PathVariable Long appointmentId) {
        long employeeId = currentUserProvider.requireEmployeeId();
        return toDto(appointmentService.cancelByEmployee(employeeId, appointmentId));
    }
}
