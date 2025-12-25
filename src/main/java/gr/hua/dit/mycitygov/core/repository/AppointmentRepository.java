package gr.hua.dit.mycitygov.core.repository;

import gr.hua.dit.mycitygov.core.model.Appointment;
import gr.hua.dit.mycitygov.core.service.model.AppointmentStatus;
import gr.hua.dit.mycitygov.core.service.model.MunicipalService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByCitizenId(Long citizenId);

    List<Appointment> findByEmployeeId(Long employeeId);

    List<Appointment> findByStatus(AppointmentStatus status);

    List<Appointment> findByAppointmentDateTimeAfter(LocalDateTime from);

    List<Appointment> findByServiceAndAppointmentDateTimeBetween(
        MunicipalService service,
        LocalDateTime start,
        LocalDateTime end
    );

    Optional<Appointment> findByServiceAndAppointmentDateTime(
        MunicipalService service,
        LocalDateTime appointmentDateTime
    );

    Optional<Appointment> findByEmployeeIdAndAppointmentDateTime(
        Long employeeId,
        LocalDateTime appointmentDateTime
    );
}
