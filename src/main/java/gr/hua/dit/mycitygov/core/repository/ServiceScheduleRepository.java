package gr.hua.dit.mycitygov.core.repository;

import gr.hua.dit.mycitygov.core.model.ServiceSchedule;
import gr.hua.dit.mycitygov.core.service.model.MunicipalService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

public interface ServiceScheduleRepository
    extends JpaRepository<ServiceSchedule, Long> {

    Optional<ServiceSchedule> findByServiceAndDayOfWeek(
        MunicipalService service,
        DayOfWeek dayOfWeek
    );

    /** Πολλαπλά διαστήματα ανά (Υπηρεσία + Ημέρα). */
    List<ServiceSchedule> findAllByServiceAndDayOfWeekOrderByStartTimeAsc(
        MunicipalService service,
        DayOfWeek dayOfWeek
    );

    /** Μόνο ενεργά ωράρια, ταξινομημένα. */
    List<ServiceSchedule> findAllByServiceAndDayOfWeekAndEnabledTrueOrderByStartTimeAsc(
        MunicipalService service,
        DayOfWeek dayOfWeek
    );

    /** Καλύτερη εμφάνιση στο admin table. */
    List<ServiceSchedule> findAllByOrderByServiceAscDayOfWeekAscStartTimeAsc();
}
