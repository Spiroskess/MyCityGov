package gr.hua.dit.mycitygov.core.service.mapper;

import gr.hua.dit.mycitygov.core.model.RequestMessage;
import gr.hua.dit.mycitygov.core.service.model.RequestMessageView;
import org.springframework.stereotype.Component;

@Component
public class RequestMessageMapper {

    public RequestMessageView toView(RequestMessage m) {
        return new RequestMessageView(
            m.getId(),
            m.getCreatedAt(),
            m.getCreatedBy(),
            m.getType(),
            m.getMessage()
        );
    }
}
