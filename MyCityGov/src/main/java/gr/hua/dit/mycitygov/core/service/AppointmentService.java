package gr.hua.dit.mycitygov.core.service;

import gr.hua.dit.mycitygov.core.model.Appointment;
import gr.hua.dit.mycitygov.core.service.model.AppointmentStatus;
import gr.hua.dit.mycitygov.core.service.model.MunicipalService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentService {

    Appointment book(Long citizenId, MunicipalService service, LocalDate date, LocalTime time);

    Appointment rescheduleByEmployee(Long employeeId, Long appointmentId, LocalDate date, LocalTime time);

    Appointment confirmByEmployee(Long employeeId, Long appointmentId);

    Appointment cancelByCitizen(Long citizenId, Long appointmentId);

    Appointment cancelByEmployee(Long employeeId, Long appointmentId);

    Appointment completeByEmployee(Long employeeId, Long appointmentId);

    List<Appointment> listForCitizen(Long citizenId);

    List<Appointment> listActiveForCitizen(Long citizenId);

    List<Appointment> listCompletedForCitizen(Long citizenId);

    List<Appointment> listForEmployee(Long employeeId);

    List<Appointment> listForAdmin();

    Appointment setStatusByAdmin(Long appointmentId, AppointmentStatus status);

    /**
     *IMPORTANT (to satisfy "no repositories in controllers"):
     * Loads an appointment and enforces access control rules for an employee.
     */
    Appointment getForEmployeeOrThrow(Long employeeId, Long appointmentId);
}
