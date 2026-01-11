package gr.hua.dit.mycitygov.core.repository;

import gr.hua.dit.mycitygov.core.model.ServiceSchedule;
import gr.hua.dit.mycitygov.core.service.model.MunicipalService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

public interface ServiceScheduleRepository
    extends JpaRepository<ServiceSchedule, Long> {

    // Εύρεση ωραρίου για συγκεκριμένη υπηρεσία και ημέρα
    Optional<ServiceSchedule> findByServiceAndDayOfWeek(
        MunicipalService service,
        DayOfWeek dayOfWeek
    );

    // Πολλαπλά διαστήματα ωραρίου ανά (υπηρεσία + ημέρα)
    List<ServiceSchedule> findAllByServiceAndDayOfWeekOrderByStartTimeAsc(
        MunicipalService service,
        DayOfWeek dayOfWeek
    );

    // Μόνο ενεργά ωράρια για δημιουργία διαθέσιμων slots
    List<ServiceSchedule> findAllByServiceAndDayOfWeekAndEnabledTrueOrderByStartTimeAsc(
        MunicipalService service,
        DayOfWeek dayOfWeek
    );

    // Ταξινόμηση για καλύτερη εμφάνιση στον admin πίνακα
    List<ServiceSchedule> findAllByOrderByServiceAscDayOfWeekAscStartTimeAsc();
}
