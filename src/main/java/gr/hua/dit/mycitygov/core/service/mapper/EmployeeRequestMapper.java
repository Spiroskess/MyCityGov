package gr.hua.dit.mycitygov.core.service.mapper;

import gr.hua.dit.mycitygov.core.model.Request;
import gr.hua.dit.mycitygov.core.service.model.EmployeeRequestView;
import org.springframework.stereotype.Component;

@Component
public class EmployeeRequestMapper {

    public EmployeeRequestView toView(Request request) {
        EmployeeRequestView view = new EmployeeRequestView();
        view.setId(request.getId());
        view.setTitle(request.getTitle());
        view.setDescription(request.getDescription());
        view.setStatus(request.getStatus().name());
        view.setSubmittedAt(request.getSubmittedAt());
        view.setTypeLabel(request.getType().getLabel());

        if (request.getCitizen() != null) {
            view.setCitizenEmail(request.getCitizen().getEmail());
            view.setCitizenName(request.getCitizen().getFullName());
        }

        if (request.getAssignedEmployee() != null) {
            view.setAssignedEmployeeEmail(request.getAssignedEmployee().getEmail());
        }

        return view;
    }
}
