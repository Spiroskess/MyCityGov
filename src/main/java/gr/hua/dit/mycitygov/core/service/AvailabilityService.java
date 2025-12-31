package gr.hua.dit.mycitygov.core.service;

import gr.hua.dit.mycitygov.core.repository.AppointmentRepository;
import gr.hua.dit.mycitygov.core.repository.ServiceScheduleRepository;
import gr.hua.dit.mycitygov.core.service.model.AppointmentStatus;
import gr.hua.dit.mycitygov.core.service.model.MunicipalService;
import gr.hua.dit.mycitygov.core.model.ServiceSchedule;
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
        return getAvailableTimes(service, date, null);
    }

    /**
     * Όπως το getAvailableTimes, αλλά μπορεί να αγνοήσει ένα συγκεκριμένο ραντεβού
     * (χρήσιμο σε reschedule ώστε να επιτρέπεται να κρατήσεις το ίδιο slot).
     */
    public List<LocalTime> getAvailableTimes(MunicipalService service, LocalDate date, Long excludeAppointmentId) {
        if (service == null) throw new IllegalArgumentException("service is null");
        if (date == null) throw new IllegalArgumentException("date is null");

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        ServiceSchedule schedule = scheduleRepository
            .findByServiceAndDayOfWeek(service, dayOfWeek)
            .orElse(null);

        if (schedule == null || !schedule.isEnabled()) {
            return List.of();
        }

        LocalDateTime start = LocalDateTime.of(date, schedule.getStartTime());
        LocalDateTime end = LocalDateTime.of(date, schedule.getEndTime());

        Set<LocalTime> booked = new HashSet<>();
        appointmentRepository.findByServiceAndAppointmentDateTimeBetween(service, start, end)
            .forEach(a -> {
                if (excludeAppointmentId != null && excludeAppointmentId.equals(a.getId())) return;
                if (a.getStatus() == AppointmentStatus.REQUESTED || a.getStatus() == AppointmentStatus.CONFIRMED) {
                    booked.add(a.getAppointmentDateTime().toLocalTime());
                }
            });

        List<LocalTime> slots = new ArrayList<>();
        LocalTime t = schedule.getStartTime();
        while (!t.isAfter(schedule.getEndTime())) {
            // να χωράει το slot (π.χ. end 15:30, slot 30 -> τελευταίο 15:00)
            LocalTime endCandidate = t.plusMinutes(schedule.getSlotMinutes());
            if (endCandidate.isAfter(schedule.getEndTime().plusSeconds(1))) break;

            if (!booked.contains(t)) {
                slots.add(t);
            }
            t = t.plusMinutes(schedule.getSlotMinutes());
        }

        return slots;
    }
}
