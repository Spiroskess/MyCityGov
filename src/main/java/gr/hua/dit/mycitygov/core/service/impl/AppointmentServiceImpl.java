package gr.hua.dit.mycitygov.core.service.impl;

import gr.hua.dit.mycitygov.core.model.Appointment;
import gr.hua.dit.mycitygov.core.model.Citizen;
import gr.hua.dit.mycitygov.core.model.RequestType;
import gr.hua.dit.mycitygov.core.repository.AppointmentRepository;
import gr.hua.dit.mycitygov.core.repository.RequestTypeRepository;
import gr.hua.dit.mycitygov.core.service.AppointmentService;
import gr.hua.dit.mycitygov.core.service.CitizenService;
import gr.hua.dit.mycitygov.core.service.mapper.AppointmentMapper;
import gr.hua.dit.mycitygov.core.service.model.AppointmentView;
import gr.hua.dit.mycitygov.core.service.model.CreateAppointmentRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AppointmentServiceImpl implements AppointmentService {

    private final CitizenService citizenService;
    private final RequestTypeRepository requestTypeRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;

    public AppointmentServiceImpl(CitizenService citizenService,
                                  RequestTypeRepository requestTypeRepository,
                                  AppointmentRepository appointmentRepository,
                                  AppointmentMapper appointmentMapper) {
        this.citizenService = citizenService;
        this.requestTypeRepository = requestTypeRepository;
        this.appointmentRepository = appointmentRepository;
        this.appointmentMapper = appointmentMapper;
    }

    @Override
    public AppointmentView createAppointment(String citizenEmail, CreateAppointmentRequest dto) {
        Citizen citizen = citizenService.getOrCreateByEmail(citizenEmail, null);

        RequestType type = requestTypeRepository.findById(dto.getRequestTypeId())
            .orElseThrow(() -> new IllegalArgumentException("RequestType not found"));

        if (appointmentRepository.existsByStartAt(dto.getStartAt())) {
            throw new IllegalStateException("Υπάρχει ήδη ραντεβού σε αυτή την ώρα");
        }

        Appointment appointment = new Appointment();
        appointment.setCitizen(citizen);
        appointment.setType(type);
        appointment.setStartAt(dto.getStartAt());
        appointment.setDurationMinutes(dto.getDurationMinutes());

        return appointmentMapper.toView(appointmentRepository.save(appointment));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentView> getCitizenAppointments(String citizenEmail) {
        Citizen citizen = citizenService.getOrCreateByEmail(citizenEmail, null);
        return appointmentRepository.findByCitizenOrderByStartAtDesc(citizen)
            .stream()
            .map(appointmentMapper::toView)
            .toList();
    }
}
