package gr.hua.dit.mycitygov.core.service;

import gr.hua.dit.mycitygov.core.repository.AppointmentRepository;
import gr.hua.dit.mycitygov.core.service.model.MunicipalService;
import gr.hua.dit.mycitygov.core.service.model.ServiceSchedule;
import gr.hua.dit.mycitygov.core.repository.ServiceScheduleRepository;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class AvailabilityService {

    private final ServiceScheduleRepository scheduleRepo;
    private final AppointmentRepository appointmentRepo;

    public AvailabilityService(ServiceScheduleRepository scheduleRepo,
                               AppointmentRepository appointmentRepo) {
        this.scheduleRepo = scheduleRepo;
        this.appointmentRepo = appointmentRepo;
    }

    public List<LocalTime> getAvailableTimes(MunicipalService service, LocalDate date) {

        Optional<ServiceSchedule> scheduleOpt =
            scheduleRepo.findByServiceAndDayOfWeek(service, date.getDayOfWeek());

        if (scheduleOpt.isEmpty()) {
            return List.of();
        }

        ServiceSchedule schedule = scheduleOpt.get();

        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.plusDays(1).atStartOfDay();

        Set<LocalTime> bookedTimes = new HashSet<>();

        appointmentRepo
            .findByServiceAndAppointmentDateTimeBetween(service, from, to)
            .forEach(a ->
                bookedTimes.add(a.getAppointmentDateTime().toLocalTime())
            );

        List<LocalTime> slots = new ArrayList<>();

        for (LocalTime t = schedule.getStartTime();
             t.plusMinutes(schedule.getSlotMinutes())
                 .compareTo(schedule.getEndTime()) <= 0;
             t = t.plusMinutes(schedule.getSlotMinutes())) {

            if (!bookedTimes.contains(t)) {
                slots.add(t);
            }
        }

        return slots;
    }
}
