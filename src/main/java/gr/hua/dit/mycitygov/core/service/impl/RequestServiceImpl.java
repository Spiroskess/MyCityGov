package gr.hua.dit.mycitygov.core.service.impl;

import gr.hua.dit.mycitygov.core.model.Citizen;
import gr.hua.dit.mycitygov.core.model.Request;
import gr.hua.dit.mycitygov.core.model.RequestStatus;
import gr.hua.dit.mycitygov.core.model.RequestType;
import gr.hua.dit.mycitygov.core.repository.RequestRepository;
import gr.hua.dit.mycitygov.core.repository.RequestTypeRepository;
import gr.hua.dit.mycitygov.core.service.CitizenService;
import gr.hua.dit.mycitygov.core.service.RequestService;
import gr.hua.dit.mycitygov.core.service.mapper.RequestMapper;
import gr.hua.dit.mycitygov.core.service.model.CreateRequestRequest;
import gr.hua.dit.mycitygov.core.service.model.RequestView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RequestServiceImpl implements RequestService {

    private final CitizenService citizenService;
    private final RequestTypeRepository requestTypeRepository;
    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;

    public RequestServiceImpl(CitizenService citizenService,
                              RequestTypeRepository requestTypeRepository,
                              RequestRepository requestRepository,
                              RequestMapper requestMapper) {
        this.citizenService = citizenService;
        this.requestTypeRepository = requestTypeRepository;
        this.requestRepository = requestRepository;
        this.requestMapper = requestMapper;
    }

    @Override
    public RequestView createRequest(String citizenEmail, CreateRequestRequest dto) {
        Citizen citizen = citizenService.getOrCreateByEmail(citizenEmail, null);

        RequestType type = requestTypeRepository.findById(dto.getRequestTypeId())
            .orElseThrow(() -> new IllegalArgumentException("RequestType not found"));

        Request request = new Request();
        request.setCitizen(citizen);
        request.setType(type);
        request.setTitle(dto.getTitle());
        request.setDescription(dto.getDescription());
        request.setStatus(RequestStatus.SUBMITTED);

        return requestMapper.toView(requestRepository.save(request));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestView> getCitizenRequests(String citizenEmail) {
        Citizen citizen = citizenService.getOrCreateByEmail(citizenEmail, null);
        return requestRepository.findByCitizenOrderBySubmittedAtDesc(citizen)
            .stream()
            .map(requestMapper::toView)
            .toList();
    }
}
