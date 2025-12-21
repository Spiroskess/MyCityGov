package gr.hua.dit.mycitygov.core.repository;

import gr.hua.dit.mycitygov.core.service.model.Appointment;
import gr.hua.dit.mycitygov.core.service.model.AppointmentStatus;
import gr.hua.dit.mycitygov.core.service.model.MunicipalService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Ραντεβού πολίτη
    List<Appointment> findByCitizenId(Long citizenId);

    // Ραντεβού υπαλλήλου
    List<Appointment> findByEmployeeId(Long employeeId);

    // Ραντεβού ανά κατάσταση
    List<Appointment> findByStatus(AppointmentStatus status);

    // Μελλοντικά ραντεβού
    List<Appointment> findByAppointmentDateTimeAfter(LocalDateTime from);

    // ✅ Για διαθέσιμες ώρες: όλα τα ραντεβού μιας υπηρεσίας μέσα σε μία μέρα
    List<Appointment> findByServiceAndAppointmentDateTimeBetween(
        MunicipalService service,
        LocalDateTime start,
        LocalDateTime end
    );

    // ✅ Για να αποτρέψεις διπλοκλείσιμο ίδιας ώρας
    Optional<Appointment> findByServiceAndAppointmentDateTime(
        MunicipalService service,
        LocalDateTime appointmentDateTime
    );
}
