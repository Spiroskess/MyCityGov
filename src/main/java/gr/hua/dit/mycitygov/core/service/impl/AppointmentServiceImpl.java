package gr.hua.dit.mycitygov.core.service.impl;

import gr.hua.dit.mycitygov.core.repository.AppointmentRepository;
import gr.hua.dit.mycitygov.core.service.AppointmentService;
import gr.hua.dit.mycitygov.core.service.AvailabilityService;
import gr.hua.dit.mycitygov.core.model.Appointment;
import gr.hua.dit.mycitygov.core.service.model.AppointmentStatus;
import gr.hua.dit.mycitygov.core.service.model.MunicipalService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AvailabilityService availabilityService;

    public AppointmentServiceImpl(AppointmentRepository appointmentRepository,
                                  AvailabilityService availabilityService) {
        this.appointmentRepository = appointmentRepository;
        this.availabilityService = availabilityService;
    }

    @Override
    @Transactional
    public Appointment book(Long citizenId, MunicipalService service, LocalDate date, LocalTime time) {
        if (citizenId == null) throw new IllegalArgumentException("citizenId is null");
        if (service == null) throw new IllegalArgumentException("service is null");
        if (date == null) throw new IllegalArgumentException("date is null");
        if (time == null) throw new IllegalArgumentException("time is null");

        // 1) validate ότι ανήκει σε διαθέσιμες ώρες (ωράριο + όχι κλεισμένο)
        var available = availabilityService.getAvailableTimes(service, date);
        if (!available.contains(time)) {
            throw new IllegalStateException("Slot not available");
        }

        LocalDateTime slot = LocalDateTime.of(date, time);

        // 2) anti double-book (τελευταία γραμμή άμυνας)
        boolean alreadyBooked = appointmentRepository
            .findByServiceAndAppointmentDateTime(service, slot)
            .filter(a -> a.getStatus() != AppointmentStatus.CANCELLED)
            .isPresent();

        if (alreadyBooked) {
            throw new IllegalStateException("Slot already booked");
        }

        Appointment a = new Appointment();
        a.setCitizenId(citizenId);
        a.setService(service);
        a.setAppointmentDateTime(slot);
        a.setStatus(AppointmentStatus.REQUESTED);

        return appointmentRepository.save(a);
    }

    @Override
    @Transactional
    public Appointment rescheduleByEmployee(Long employeeId, Long appointmentId, LocalDate date, LocalTime time) {
        if (employeeId == null) throw new IllegalArgumentException("employeeId is null");
        if (appointmentId == null) throw new IllegalArgumentException("appointmentId is null");

        Appointment a = appointmentRepository.findById(appointmentId).orElseThrow();

        LocalDateTime newSlot = LocalDateTime.of(date, time);

        // validate availability
        var available = availabilityService.getAvailableTimes(a.getService(), date);
        if (!available.contains(time)) {
            throw new IllegalStateException("Slot not available");
        }

        // overlap rule: ίδιος υπάλληλος - όχι ίδια ώρα
        if (a.getEmployeeId() != null) {
            boolean clash = appointmentRepository
                .findByEmployeeIdAndAppointmentDateTime(a.getEmployeeId(), newSlot)
                .filter(x -> !x.getId().equals(a.getId()))
                .filter(x -> x.getStatus() != AppointmentStatus.CANCELLED)
                .isPresent();

            if (clash) {
                throw new IllegalStateException("Employee overlap");
            }
        } else {
            // αν δεν είχε υπάλληλο, τον “αναθέτουμε” σε αυτόν που το χειρίζεται
            a.setEmployeeId(employeeId);
        }

        a.setAppointmentDateTime(newSlot);
        return appointmentRepository.save(a);
    }

    @Override
    @Transactional
    public Appointment confirmByEmployee(Long employeeId, Long appointmentId) {
        if (employeeId == null) throw new IllegalArgumentException("employeeId is null");
        Appointment a = appointmentRepository.findById(appointmentId).orElseThrow();

        if (a.getEmployeeId() == null) {
            a.setEmployeeId(employeeId);
        } else if (!a.getEmployeeId().equals(employeeId)) {
            throw new IllegalStateException("Not your appointment");
        }

        if (a.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Cannot confirm cancelled appointment");
        }

        a.setStatus(AppointmentStatus.CONFIRMED);
        return appointmentRepository.save(a);
    }

    @Override
    @Transactional
    public Appointment cancelByCitizen(Long citizenId, Long appointmentId) {
        Appointment a = appointmentRepository.findById(appointmentId).orElseThrow();
        if (!citizenId.equals(a.getCitizenId())) {
            throw new IllegalStateException("Citizen cannot cancel others appointment");
        }
        if (a.getStatus() == AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed");
        }
        a.setStatus(AppointmentStatus.CANCELLED);
        return appointmentRepository.save(a);
    }

    @Override
    @Transactional
    public Appointment cancelByEmployee(Long employeeId, Long appointmentId) {
        Appointment a = appointmentRepository.findById(appointmentId).orElseThrow();

        if (a.getEmployeeId() != null && !a.getEmployeeId().equals(employeeId)) {
            throw new IllegalStateException("Not your appointment");
        }

        if (a.getStatus() == AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed");
        }

        a.setEmployeeId(employeeId); // ο υπάλληλος που το ακύρωσε
        a.setStatus(AppointmentStatus.CANCELLED);
        return appointmentRepository.save(a);
    }

    @Override
    public List<Appointment> listForCitizen(Long citizenId) {
        return appointmentRepository.findByCitizenId(citizenId);
    }

    @Override
    public List<Appointment> listForEmployee(Long employeeId) {
        return appointmentRepository.findByEmployeeId(employeeId);
    }

    @Override
    public List<Appointment> listForAdmin() {
        return appointmentRepository.findAll();
    }
}
