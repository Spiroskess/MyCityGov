package gr.hua.dit.mycitygov.core.service;

import gr.hua.dit.mycitygov.core.model.ServiceSchedule;
import gr.hua.dit.mycitygov.core.repository.AppointmentRepository;
import gr.hua.dit.mycitygov.core.repository.ServiceScheduleRepository;
import gr.hua.dit.mycitygov.core.service.model.AppointmentStatus;
import gr.hua.dit.mycitygov.core.service.model.MunicipalService;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;

@Service
public class AvailabilityService {

    private final AppointmentRepository appointmentRepository;
    private final ServiceScheduleRepository scheduleRepository;

    public AvailabilityService(AppointmentRepository appointmentRepository,
                               ServiceScheduleRepository scheduleRepository) {
        this.appointmentRepository = appointmentRepository;
        this.scheduleRepository = scheduleRepository;
    }

    public List<LocalTime> getAvailableTimes(MunicipalService service, LocalDate date) {
        // Υπολογίζει διαθέσιμα time slots για μια υπηρεσία και ημερομηνία
        return getAvailableTimes(service, date, null);
    }

    /**
     * Όπως το getAvailableTimes, αλλά μπορεί να αγνοήσει ένα συγκεκριμένο ραντεβού
     */
    public List<LocalTime> getAvailableTimes(MunicipalService service, LocalDate date, Long excludeAppointmentId) {
        // Συνδυάζει ωράρια (ServiceSchedule) + ήδη κλεισμένα ραντεβού για να βγάλει διαθέσιμα slots
        if (service == null) throw new IllegalArgumentException("service is null");
        if (date == null) throw new IllegalArgumentException("date is null");

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        List<ServiceSchedule> schedules = scheduleRepository
            .findAllByServiceAndDayOfWeekAndEnabledTrueOrderByStartTimeAsc(service, dayOfWeek);

        if (schedules.isEmpty()) {
            return List.of();
        }

        // Παίρνει ένα "εύρος ημέρας" ώστε να κάνει 1 query για τα ραντεβού της ημέρας
        LocalTime minStart = schedules.get(0).getStartTime();
        LocalTime maxEnd = schedules.get(0).getEndTime();
        for (ServiceSchedule s : schedules) {
            if (s.getStartTime().isBefore(minStart)) minStart = s.getStartTime();
            if (s.getEndTime().isAfter(maxEnd)) maxEnd = s.getEndTime();
        }

        LocalDateTime start = LocalDateTime.of(date, minStart);
        LocalDateTime end = LocalDateTime.of(date, maxEnd);

        // “Κλεισμένα” slots (μόνο REQUESTED/CONFIRMED) για να μην προταθούν ξανά
        Set<LocalTime> booked = new HashSet<>();
        appointmentRepository.findByServiceAndAppointmentDateTimeBetween(service, start, end)
            .forEach(a -> {
                if (excludeAppointmentId != null && excludeAppointmentId.equals(a.getId())) return;
                if (a.getStatus() == AppointmentStatus.REQUESTED || a.getStatus() == AppointmentStatus.CONFIRMED) {
                    booked.add(a.getAppointmentDateTime().toLocalTime());
                }
            });

        // Δημιουργεί slots ανά διάστημα ωραρίου (start->end) με βήμα slotMinutes
        List<LocalTime> slots = new ArrayList<>();
        for (ServiceSchedule s : schedules) {
            int step = s.getSlotMinutes() > 0 ? s.getSlotMinutes() : 15;
            LocalTime t = s.getStartTime();
            while (!t.plusMinutes(step).isAfter(s.getEndTime())) {
                if (!booked.contains(t)) slots.add(t);
                t = t.plusMinutes(step);
            }
        }

        return slots.stream().distinct().sorted().toList();
    }
}
