package gr.hua.dit.mycitygov.core.service.mapper;

import gr.hua.dit.mycitygov.core.model.Appointment;
import gr.hua.dit.mycitygov.core.service.model.AppointmentView;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {

    public AppointmentView toView(Appointment appointment) {
        AppointmentView view = new AppointmentView();
        view.setId(appointment.getId());
        view.setStartAt(appointment.getStartAt());
        view.setDurationMinutes(appointment.getDurationMinutes());
        view.setTypeLabel(appointment.getType().getLabel());
        return view;
    }
}
