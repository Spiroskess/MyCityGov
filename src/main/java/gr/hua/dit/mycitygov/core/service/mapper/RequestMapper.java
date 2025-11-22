package gr.hua.dit.mycitygov.core.service.mapper;

import gr.hua.dit.mycitygov.core.model.Request;
import gr.hua.dit.mycitygov.core.service.model.RequestView;
import org.springframework.stereotype.Component;

@Component
public class RequestMapper {

    public RequestView toView(Request request) {
        RequestView view = new RequestView();
        view.setId(request.getId());
        view.setTitle(request.getTitle());
        view.setDescription(request.getDescription());
        view.setStatus(request.getStatus().name());
        view.setSubmittedAt(request.getSubmittedAt());
        view.setTypeLabel(request.getType().getLabel());
        return view;
    }
}
