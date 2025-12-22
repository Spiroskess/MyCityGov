package gr.hua.dit.mycitygov.core.service.impl;

import gr.hua.dit.mycitygov.core.repository.ServiceScheduleRepository;
import gr.hua.dit.mycitygov.core.service.AdminScheduleService;
import gr.hua.dit.mycitygov.core.model.ServiceSchedule;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminScheduleServiceImpl implements AdminScheduleService {

    private final ServiceScheduleRepository repo;

    public AdminScheduleServiceImpl(ServiceScheduleRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<ServiceSchedule> findAll() {
        return repo.findAll();
    }

    @Override
    @Transactional
    public ServiceSchedule create(ServiceSchedule schedule) {
        if (schedule == null) {
            throw new IllegalArgumentException("schedule is null");
        }
        if (schedule.getService() == null) {
            throw new IllegalArgumentException("Διάλεξε υπηρεσία.");
        }
        if (schedule.getDayOfWeek() == null) {
            throw new IllegalArgumentException("Διάλεξε ημέρα.");
        }
        if (schedule.getStartTime() == null || schedule.getEndTime() == null) {
            throw new IllegalArgumentException("Συμπλήρωσε start/end ώρα.");
        }
        if (schedule.getSlotMinutes() <= 0) {
            throw new IllegalArgumentException("Το slotMinutes πρέπει να είναι > 0.");
        }
        if (schedule.getStartTime().isAfter(schedule.getEndTime())) {
            throw new IllegalArgumentException("Το startTime πρέπει να είναι πριν από το endTime.");
        }

        // να μην επιτρέπεις duplicate (service + dayOfWeek) schedules
        repo.findByServiceAndDayOfWeek(schedule.getService(), schedule.getDayOfWeek())
            .ifPresent(existing -> {
                throw new IllegalStateException("Υπάρχει ήδη ωράριο για αυτή την υπηρεσία και ημέρα.");
            });

        return repo.save(schedule);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (id == null) throw new IllegalArgumentException("id is null");
        repo.deleteById(id);
    }
}
