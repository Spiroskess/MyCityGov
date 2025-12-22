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
        DayOfWeek day = date.getDayOfWeek();

        ServiceSchedule schedule = scheduleRepository
            .findByServiceAndDayOfWeek(service, day)
            .orElse(null);

        if (schedule == null) return List.of();

        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.plusDays(1).atStartOfDay();

        Set<LocalTime> booked = new HashSet<>();
        appointmentRepository.findByServiceAndAppointmentDateTimeBetween(service, from, to)
            .stream()
            .filter(a -> a.getStatus() == AppointmentStatus.REQUESTED
                || a.getStatus() == AppointmentStatus.CONFIRMED)
            .forEach(a -> booked.add(a.getAppointmentDateTime().toLocalTime()));

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
