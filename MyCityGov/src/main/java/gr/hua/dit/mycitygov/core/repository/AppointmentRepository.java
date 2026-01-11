package gr.hua.dit.mycitygov.core.repository;

import gr.hua.dit.mycitygov.core.model.Appointment;
import gr.hua.dit.mycitygov.core.service.model.AppointmentStatus;
import gr.hua.dit.mycitygov.core.service.model.MunicipalService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    // Queries για ραντεβού ανά πολίτη/υπάλληλο/υπηρεσία
    List<Appointment> findByCitizenId(Long citizenId);

    List<Appointment> findByCitizenIdAndStatusInOrderByAppointmentDateTimeDesc(
        Long citizenId,
        Collection<AppointmentStatus> statuses
    );

    List<Appointment> findByEmployeeIdAndStatusInOrderByAppointmentDateTimeDesc(
        Long employeeId,
        Collection<AppointmentStatus> statuses
    );

    List<Appointment> findByStatus(AppointmentStatus status);

    List<Appointment> findByAppointmentDateTimeAfter(LocalDateTime from);

    List<Appointment> findByServiceAndAppointmentDateTimeBetween(
        MunicipalService service,
        LocalDateTime start,
        LocalDateTime end
    );

    List<Appointment> findByServiceAndStatusInOrderByAppointmentDateTimeDesc(
        MunicipalService service,
        Collection<AppointmentStatus> statuses
    );

    Optional<Appointment> findFirstByCitizenIdAndServiceAndStatusInOrderByAppointmentDateTimeDesc(
        Long citizenId,
        MunicipalService service,
        Collection<AppointmentStatus> statuses
    );

    // Έλεγχος για να μην υπάρχουν επικαλυπτόμενα ραντεβού στην ίδια υπηρεσία (ίδιο slot)
    @Query("""
        select (count(a) > 0)
        from Appointment a
        where a.service = :service
          and a.appointmentDateTime = :slot
          and a.status in :activeStatuses
          and (:excludeId is null or a.id <> :excludeId)
        """)
    boolean existsActiveServiceClash(
        @Param("service") MunicipalService service,
        @Param("slot") LocalDateTime slot,
        @Param("activeStatuses") Collection<AppointmentStatus> activeStatuses,
        @Param("excludeId") Long excludeId
    );

    // Έλεγχος για να μην έχει ο ίδιος υπάλληλος 2 ραντεβού στο ίδιο slot
    @Query("""
        select (count(a) > 0)
        from Appointment a
        where a.employeeId = :employeeId
          and a.appointmentDateTime = :slot
          and a.status in :activeStatuses
          and (:excludeId is null or a.id <> :excludeId)
        """)
    boolean existsActiveEmployeeClash(
        @Param("employeeId") Long employeeId,
        @Param("slot") LocalDateTime slot,
        @Param("activeStatuses") Collection<AppointmentStatus> activeStatuses,
        @Param("excludeId") Long excludeId
    );
}
