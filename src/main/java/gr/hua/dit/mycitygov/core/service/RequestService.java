package gr.hua.dit.mycitygov.core.service;

import gr.hua.dit.mycitygov.core.service.model.CreateRequestRequest;
import gr.hua.dit.mycitygov.core.service.model.RequestView;

import java.util.List;

public interface RequestService {

    RequestView createRequest(String citizenEmail, CreateRequestRequest dto);

    List<RequestView> getCitizenRequests(String citizenEmail);
}
