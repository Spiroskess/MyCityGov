package gr.hua.dit.mycitygov.core.service.impl;


import gr.hua.dit.mycitygov.core.model.Person;
import gr.hua.dit.mycitygov.core.model.Request;
import gr.hua.dit.mycitygov.core.model.RequestStatus;
import gr.hua.dit.mycitygov.core.repository.RequestRepository;
import gr.hua.dit.mycitygov.core.service.RequestService;
import gr.hua.dit.mycitygov.core.service.mapper.RequestMapper;
import gr.hua.dit.mycitygov.core.service.model.OpenRequestRequest;
import gr.hua.dit.mycitygov.core.service.model.RequestView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;

    public RequestServiceImpl(RequestRepository requestRepository,
                              RequestMapper requestMapper) {
        this.requestRepository = requestRepository;
        this.requestMapper = requestMapper;
    }

    @Override
    @Transactional
    public RequestView openRequest(Person citizen, OpenRequestRequest openReq) {
        Request request = new Request();
        request.setCitizen(citizen);
        request.setType(openReq.type());
        request.setSubject(openReq.subject());
        request.setDescription(openReq.description());
        request.setStatus(RequestStatus.SUBMITTED);
        request.setProtocolNumber(generateProtocolNumber());
        request.setCreatedAt(Instant.now());
        request.setUpdatedAt(Instant.now());
        request = requestRepository.save(request);
        return requestMapper.convertRequestToView(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestView> getRequestsOfCitizen(Person citizen) {
        return requestRepository.findAllByCitizenOrderByCreatedAtDesc(citizen)
            .stream()
            .map(requestMapper::convertRequestToView)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestView> getRequestsAssignedToEmployee(Person employee) {
        return requestRepository.findAllByAssignedEmployeeOrderByCreatedAtDesc(employee)
            .stream()
            .map(requestMapper::convertRequestToView)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestView> getAllRequests() {
        return requestRepository.findAll()
            .stream()
            .map(requestMapper::convertRequestToView)
            .toList();
    }

    @Override
    @Transactional
    public Optional<RequestView> assignRequestToEmployee(Long requestId, Person employee) {
        return requestRepository.findById(requestId)
            .map(request -> {
                request.setAssignedEmployee(employee);
                request.setStatus(RequestStatus.RECEIVED);
                request.setUpdatedAt(Instant.now());
                return requestMapper.convertRequestToView(request);
            });
    }

    private String generateProtocolNumber() {
        // απλό παράδειγμα – μπορείς να το αλλάξεις
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
