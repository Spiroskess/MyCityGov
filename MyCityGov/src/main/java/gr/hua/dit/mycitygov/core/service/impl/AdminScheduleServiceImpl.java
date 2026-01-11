package gr.hua.dit.mycitygov.core.service.impl;

import gr.hua.dit.mycitygov.core.model.ServiceSchedule;
import gr.hua.dit.mycitygov.core.repository.ServiceScheduleRepository;
import gr.hua.dit.mycitygov.core.service.AdminScheduleService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@Service
public class AdminScheduleServiceImpl implements AdminScheduleService {

    private final ServiceScheduleRepository repo;

    public AdminScheduleServiceImpl(ServiceScheduleRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<ServiceSchedule> findAll() {
        // Επιστρέφει όλα τα ωράρια ταξινομημένα για admin πίνακα
        return repo.findAllByOrderByServiceAscDayOfWeekAscStartTimeAsc();
    }

    @Override
    @Transactional
    public ServiceSchedule create(ServiceSchedule schedule) {
        // Business validation για ωράρια + κανόνας "όχι επικαλύψεις" (overlaps)
        if (schedule == null) throw new IllegalArgumentException("schedule is null");
        if (schedule.getService() == null) throw new IllegalArgumentException("Διάλεξε υπηρεσία.");
        if (schedule.getDayOfWeek() == null) throw new IllegalArgumentException("Διάλεξε ημέρα.");
        if (schedule.getStartTime() == null || schedule.getEndTime() == null) {
            throw new IllegalArgumentException("Συμπλήρωσε 'Από' και 'Έως'.");
        }
        if (schedule.getSlotMinutes() <= 0) {
            throw new IllegalArgumentException("Το slot πρέπει να είναι > 0 λεπτά.");
        }
        if (!schedule.getStartTime().isBefore(schedule.getEndTime())) {
            throw new IllegalArgumentException("Η ώρα 'Από' πρέπει να είναι πριν από την ώρα 'Έως'.");
        }

        // Δεν επιτρέπονται επικαλυπτόμενα ωράρια στην ίδια (Υπηρεσία + Ημέρα)
        List<ServiceSchedule> existing = repo.findAllByServiceAndDayOfWeekOrderByStartTimeAsc(
            schedule.getService(), schedule.getDayOfWeek()
        );

        for (ServiceSchedule ex : existing) {
            if (schedule.getId() != null && schedule.getId().equals(ex.getId())) continue;

            if (overlaps(schedule.getStartTime(), schedule.getEndTime(), ex.getStartTime(), ex.getEndTime())) {
                throw new IllegalStateException(
                    "Υπάρχει επικάλυψη με άλλο ωράριο της ίδιας υπηρεσίας/ημέρας (" +
                        ex.getStartTime() + "–" + ex.getEndTime() + ")."
                );
            }
        }

        return repo.save(schedule);
    }

    private boolean overlaps(LocalTime startA, LocalTime endA, LocalTime startB, LocalTime endB) {
        // overlap αν startA < endB && endA > startB (αν ακουμπάνε ακριβώς, δεν είναι overlap)
        return startA.isBefore(endB) && endA.isAfter(startB);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        // Διαγραφή ωραρίου από admin
        if (id == null) throw new IllegalArgumentException("id is null");
        repo.deleteById(id);
    }
}
