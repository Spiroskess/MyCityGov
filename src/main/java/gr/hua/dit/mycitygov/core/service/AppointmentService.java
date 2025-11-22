package gr.hua.dit.mycitygov.core.service;

import gr.hua.dit.mycitygov.core.service.model.AppointmentView;
import gr.hua.dit.mycitygov.core.service.model.CreateAppointmentRequest;

import java.util.List;

public interface AppointmentService {

    AppointmentView createAppointment(String citizenEmail, CreateAppointmentRequest dto);

    List<AppointmentView> getCitizenAppointments(String citizenEmail);
}

