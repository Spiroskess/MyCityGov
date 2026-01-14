package gr.hua.dit.mycitygov.core.service.impl;

import gr.hua.dit.mycitygov.core.model.Appointment;
import gr.hua.dit.mycitygov.core.model.Person;
import gr.hua.dit.mycitygov.core.repository.AppointmentRepository;
import gr.hua.dit.mycitygov.core.repository.PersonRepository;
import gr.hua.dit.mycitygov.core.service.AppointmentService;
import gr.hua.dit.mycitygov.core.service.AvailabilityService;
import gr.hua.dit.mycitygov.core.service.model.AppointmentStatus;
import gr.hua.dit.mycitygov.core.service.model.MunicipalService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;

@Service
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AvailabilityService availabilityService;
    private final PersonRepository personRepository;

    private static final EnumSet<AppointmentStatus> ACTIVE_STATUSES =
        EnumSet.of(AppointmentStatus.REQUESTED, AppointmentStatus.CONFIRMED);

    private static final EnumSet<AppointmentStatus> COMPLETED_STATUSES =
        EnumSet.of(AppointmentStatus.CANCELLED, AppointmentStatus.COMPLETED);

    public AppointmentServiceImpl(AppointmentRepository appointmentRepository,
                                  AvailabilityService availabilityService,
                                  PersonRepository personRepository) {
        this.appointmentRepository = appointmentRepository;
        this.availabilityService = availabilityService;
        this.personRepository = personRepository;
    }

    private Person requireEmployee(Long employeeId) {
        return personRepository.findById(employeeId)
            .orElseThrow(() -> new IllegalStateException("Employee not found: " + employeeId));
    }

    /**
     * Access rules:
     * - If employee has municipalService -> can access appointments of that service (even if unassigned).
     * - If employee has NO municipalService -> can access only appointments already assigned to them.
     */
    private void ensureEmployeeCanAccess(Person employee, Appointment a) {
        final MunicipalService svc = employee.getMunicipalService();

        if (svc != null) {
            if (a.getService() != svc) {
                throw new IllegalStateException("Δεν επιτρέπεται πρόσβαση σε ραντεβού άλλης υπηρεσίας");
            }
            return;
        }

        // no service => only own assigned appointments
        if (a.getEmployeeId() == null || !a.getEmployeeId().equals(employee.getId())) {
            throw new IllegalStateException("Not your appointment");
        }
    }

    @Override
    @Transactional
    public Appointment book(Long citizenId, MunicipalService service, LocalDate date, LocalTime time) {
        if (citizenId == null) throw new IllegalArgumentException("citizenId is null");
        if (service == null) throw new IllegalArgumentException("service is null");
        if (date == null) throw new IllegalArgumentException("date is null");
        if (time == null) throw new IllegalArgumentException("time is null");

        if (appointmentRepository
            .findFirstByCitizenIdAndServiceAndStatusInOrderByAppointmentDateTimeDesc(citizenId, service, ACTIVE_STATUSES)
            .isPresent()) {
            throw new IllegalStateException("Έχεις ήδη ενεργό ραντεβού για αυτή την υπηρεσία.");
        }

        List<LocalTime> available = availabilityService.getAvailableTimes(service, date);
        if (!available.contains(time)) {
            throw new IllegalStateException("Time slot not available");
        }

        LocalDateTime slot = LocalDateTime.of(date, time);

        if (appointmentRepository.existsActiveServiceClash(service, slot, ACTIVE_STATUSES, null)) {
            throw new IllegalStateException("Service overlap");
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
        if (date == null) throw new IllegalArgumentException("date is null");
        if (time == null) throw new IllegalArgumentException("time is null");

        Appointment a = appointmentRepository.findById(appointmentId).orElseThrow();
        Person employee = requireEmployee(employeeId);

        ensureEmployeeCanAccess(employee, a);

        if (a.getEmployeeId() != null && !a.getEmployeeId().equals(employeeId)) {
            throw new IllegalStateException("Not your appointment");
        }

        if (a.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Cannot reschedule cancelled appointment");
        }
        if (a.getStatus() == AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Cannot reschedule completed appointment");
        }

        List<LocalTime> available = availabilityService.getAvailableTimes(a.getService(), date, a.getId());
        if (!available.contains(time)) {
            throw new IllegalStateException("Time slot not available");
        }

        LocalDateTime newSlot = LocalDateTime.of(date, time);

        if (appointmentRepository.existsActiveServiceClash(a.getService(), newSlot, ACTIVE_STATUSES, a.getId())) {
            throw new IllegalStateException("Service overlap");
        }

        if (appointmentRepository.existsActiveEmployeeClash(employeeId, newSlot, ACTIVE_STATUSES, a.getId())) {
            throw new IllegalStateException("Employee overlap");
        }

        // if employee has service, allow claiming unassigned appointment
        if (a.getEmployeeId() == null && employee.getMunicipalService() != null) {
            a.setEmployeeId(employeeId);
        }

        a.setAppointmentDateTime(newSlot);
        return appointmentRepository.save(a);
    }

    @Override
    @Transactional
    public Appointment confirmByEmployee(Long employeeId, Long appointmentId) {
        if (employeeId == null) throw new IllegalArgumentException("employeeId is null");
        if (appointmentId == null) throw new IllegalArgumentException("appointmentId is null");

        Appointment a = appointmentRepository.findById(appointmentId).orElseThrow();
        Person employee = requireEmployee(employeeId);

        ensureEmployeeCanAccess(employee, a);

        if (appointmentRepository.existsActiveEmployeeClash(
            employeeId, a.getAppointmentDateTime(), ACTIVE_STATUSES, a.getId()
        )) {
            throw new IllegalStateException("Employee overlap");
        }

        if (a.getEmployeeId() == null && employee.getMunicipalService() != null) {
            a.setEmployeeId(employeeId);
        } else if (a.getEmployeeId() != null && !a.getEmployeeId().equals(employeeId)) {
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
        if (citizenId == null) throw new IllegalArgumentException("citizenId is null");
        if (appointmentId == null) throw new IllegalArgumentException("appointmentId is null");

        Appointment a = appointmentRepository.findById(appointmentId).orElseThrow();
        if (!citizenId.equals(a.getCitizenId())) {
            throw new IllegalStateException("Not your appointment");
        }
        if (a.getStatus() == AppointmentStatus.CANCELLED) {
            return a;
        }
        if (a.getStatus() == AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed appointment");
        }

        a.setStatus(AppointmentStatus.CANCELLED);
        return appointmentRepository.save(a);
    }

    @Override
    @Transactional
    public Appointment cancelByEmployee(Long employeeId, Long appointmentId) {
        if (employeeId == null) throw new IllegalArgumentException("employeeId is null");
        if (appointmentId == null) throw new IllegalArgumentException("appointmentId is null");

        Appointment a = appointmentRepository.findById(appointmentId).orElseThrow();
        Person employee = requireEmployee(employeeId);

        ensureEmployeeCanAccess(employee, a);

        if (a.getStatus() == AppointmentStatus.CANCELLED) {
            return a;
        }
        if (a.getStatus() == AppointmentStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed appointment");
        }

        if (a.getEmployeeId() == null && employee.getMunicipalService() != null) {
            a.setEmployeeId(employeeId);
        } else if (a.getEmployeeId() != null && !a.getEmployeeId().equals(employeeId)) {
            throw new IllegalStateException("Not your appointment");
        }

        a.setStatus(AppointmentStatus.CANCELLED);
        return appointmentRepository.save(a);
    }

    @Override
    @Transactional
    public Appointment completeByEmployee(Long employeeId, Long appointmentId) {
        if (employeeId == null) throw new IllegalArgumentException("employeeId is null");
        if (appointmentId == null) throw new IllegalArgumentException("appointmentId is null");

        Appointment a = appointmentRepository.findById(appointmentId).orElseThrow();
        Person employee = requireEmployee(employeeId);

        ensureEmployeeCanAccess(employee, a);

        if (a.getStatus() == AppointmentStatus.CANCELLED) {
            throw new IllegalStateException("Cannot complete cancelled appointment");
        }

        if (a.getEmployeeId() == null && employee.getMunicipalService() != null) {
            a.setEmployeeId(employeeId);
        } else if (a.getEmployeeId() != null && !a.getEmployeeId().equals(employeeId)) {
            throw new IllegalStateException("Not your appointment");
        }

        a.setStatus(AppointmentStatus.COMPLETED);
        return appointmentRepository.save(a);
    }

    @Override
    public List<Appointment> listForCitizen(Long citizenId) {
        if (citizenId == null) throw new IllegalArgumentException("citizenId is null");
        return appointmentRepository.findByCitizenId(citizenId);
    }

    @Override
    public List<Appointment> listActiveForCitizen(Long citizenId) {
        if (citizenId == null) throw new IllegalArgumentException("citizenId is null");
        return appointmentRepository.findByCitizenIdAndStatusInOrderByAppointmentDateTimeDesc(
            citizenId, ACTIVE_STATUSES
        );
    }

    @Override
    public List<Appointment> listCompletedForCitizen(Long citizenId) {
        if (citizenId == null) throw new IllegalArgumentException("citizenId is null");
        return appointmentRepository.findByCitizenIdAndStatusInOrderByAppointmentDateTimeDesc(
            citizenId, COMPLETED_STATUSES
        );
    }

    @Override
    public List<Appointment> listForEmployee(Long employeeId) {
        if (employeeId == null) throw new IllegalArgumentException("employeeId is null");

        Person employee = requireEmployee(employeeId);
        MunicipalService svc = employee.getMunicipalService();

        if (svc == null) {
            return appointmentRepository.findByEmployeeIdAndStatusInOrderByAppointmentDateTimeDesc(
                employeeId, EnumSet.allOf(AppointmentStatus.class)
            );
        }

        return appointmentRepository.findByServiceAndStatusInOrderByAppointmentDateTimeDesc(
            svc,
            ACTIVE_STATUSES
        );
    }

    @Override
    public List<Appointment> listForAdmin() {
        return appointmentRepository.findAll();
    }

    @Override
    @Transactional
    public Appointment setStatusByAdmin(Long appointmentId, AppointmentStatus status) {
        if (appointmentId == null) throw new IllegalArgumentException("appointmentId is null");
        if (status == null) throw new IllegalArgumentException("status is null");

        Appointment a = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new IllegalStateException("Appointment not found: " + appointmentId));

        a.setStatus(status);
        return appointmentRepository.save(a);
    }

    @Override
    @Transactional
    public Appointment getForEmployeeOrThrow(Long employeeId, Long appointmentId) {
        if (employeeId == null) throw new IllegalArgumentException("employeeId is null");
        if (appointmentId == null) throw new IllegalArgumentException("appointmentId is null");

        Appointment a = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new IllegalStateException("Appointment not found: " + appointmentId));

        Person employee = requireEmployee(employeeId);
        ensureEmployeeCanAccess(employee, a);
        return a;
    }
}
