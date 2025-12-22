package gr.hua.dit.mycitygov.core.repository;

import gr.hua.dit.mycitygov.core.service.model.MunicipalService;
import gr.hua.dit.mycitygov.core.model.ServiceSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.Optional;

public interface ServiceScheduleRepository
    extends JpaRepository<ServiceSchedule, Long> {

    Optional<ServiceSchedule> findByServiceAndDayOfWeek(
        MunicipalService service,
        DayOfWeek dayOfWeek
    );
}
