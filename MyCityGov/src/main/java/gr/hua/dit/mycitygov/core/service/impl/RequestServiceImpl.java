package gr.hua.dit.mycitygov.core.service.impl;

import gr.hua.dit.mycitygov.core.model.*;
import gr.hua.dit.mycitygov.core.port.SmsNotificationPort;
import gr.hua.dit.mycitygov.core.repository.RequestMessageRepository;
import gr.hua.dit.mycitygov.core.repository.RequestRepository;
import gr.hua.dit.mycitygov.core.service.RequestService;
import gr.hua.dit.mycitygov.core.service.RequestStatusTransitions;
import gr.hua.dit.mycitygov.core.service.mapper.RequestMapper;
import gr.hua.dit.mycitygov.core.service.mapper.RequestMessageMapper;
import gr.hua.dit.mycitygov.core.service.model.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RequestServiceImpl implements RequestService {

    private final RequestRepository requestRepository;
    private final RequestMessageRepository requestMessageRepository;
    private final RequestMapper requestMapper;
    private final RequestMessageMapper requestMessageMapper;
    private final SmsNotificationPort smsNotificationPort;

    public RequestServiceImpl(
        RequestRepository requestRepository,
        RequestMessageRepository requestMessageRepository,
        RequestMapper requestMapper,
        RequestMessageMapper requestMessageMapper,
        SmsNotificationPort smsNotificationPort
    ) {
        this.requestRepository = requestRepository;
        this.requestMessageRepository = requestMessageRepository;
        this.requestMapper = requestMapper;
        this.requestMessageMapper = requestMessageMapper;
        this.smsNotificationPort = smsNotificationPort;
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

        request.setSlaDueDate(calculateSlaDueDate(openReq.type()));
        request.setAssignedService(null);

        Request saved = requestRepository.save(request);
        return requestMapper.convertRequestToView(saved);
    }

    private LocalDate calculateSlaDueDate(RequestType type) {
        LocalDate today = LocalDate.now();
        return switch (type) {
            case CERTIFICATE_RESIDENCE -> today.plusDays(10);
            case SIDEWALK_LICENSE      -> today.plusDays(15);
            case LIGHTING_ISSUE        -> today.plusDays(7);
            case ROAD_HOLE             -> today.plusDays(7);
            case CLEANING_ISSUE        -> today.plusDays(5);
            case OTHER                 -> today.plusDays(20);
        };
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
    public Optional<RequestView> getCitizenRequestDetails(Long requestId, Person citizen) {
        return requestRepository.findByIdAndCitizen(requestId, citizen)
            .map(requestMapper::convertRequestToView);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestMessageView> getCitizenMessages(Long requestId, Person citizen) {
        Request req = requestRepository.findByIdAndCitizen(requestId, citizen)
            .orElseThrow(() -> new IllegalStateException("Request not found"));

        return requestMessageRepository
            .findAllByRequestAndVisibleToCitizenOrderByCreatedAtAsc(req, true)
            .stream()
            .map(requestMessageMapper::toView)
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
        return requestRepository.findAllByOrderByCreatedAtDesc()
            .stream()
            .map(requestMapper::convertRequestToView)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestView> getUnassignedRequests() {
        return requestRepository.findAllByAssignedServiceIsNullOrderByCreatedAtDesc()
            .stream()
            .map(requestMapper::convertRequestToView)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestView> getAssignedRequests() {
        return requestRepository.findAllByAssignedServiceIsNotNullOrderByCreatedAtDesc()
            .stream()
            .map(requestMapper::convertRequestToView)
            .toList();
    }

    @Override
    @Transactional
    public Optional<RequestView> assignRequestToService(Long requestId, MunicipalService service) {
        return requestRepository.findById(requestId)
            .map(request -> {
                request.setAssignedService(service);

                if (request.getStatus() == RequestStatus.SUBMITTED) {
                    request.setStatus(RequestStatus.RECEIVED);
                }

                request.setUpdatedAt(Instant.now());
                Request saved = requestRepository.save(request);
                return requestMapper.convertRequestToView(saved);
            });
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestView> getServiceQueue(MunicipalService service) {
        if (service == null) {
            return List.of();
        }

        return requestRepository
            .findAllByAssignedServiceAndAssignedEmployeeIsNullOrderByCreatedAtDesc(service)
            .stream()
            .map(requestMapper::convertRequestToView)
            .toList();
    }

    @Override
    @Transactional
    public Optional<RequestView> claimRequest(Long requestId, Person employee) {
        if (employee.getRole() != PersonRole.EMPLOYEE) return Optional.empty();
        if (employee.getMunicipalService() == null) return Optional.empty();

        return requestRepository.findById(requestId)
            .filter(req -> req.getAssignedService() != null)
            .filter(req -> req.getAssignedService() == employee.getMunicipalService())
            .filter(req -> req.getAssignedEmployee() == null)
            .map(req -> {
                req.setAssignedEmployee(employee);
                req.setUpdatedAt(Instant.now());
                Request saved = requestRepository.save(req);
                return requestMapper.convertRequestToView(saved);
            });
    }

    @Override
    @Transactional
    public Optional<RequestView> updateStatus(Long requestId, Person employee, RequestStatus nextStatus, String comment) {
        return requestRepository.findById(requestId)
            .filter(req -> req.getAssignedEmployee() != null && req.getAssignedEmployee().getId().equals(employee.getId()))
            .filter(req -> RequestStatusTransitions.canMove(req.getStatus(), nextStatus))
            .map(req -> {

                if (requiresComment(nextStatus)) {
                    if (comment == null || comment.trim().isEmpty()) {
                        throw new IllegalArgumentException("COMMENT_REQUIRED");
                    }
                }

                req.setStatus(nextStatus);
                req.setStatusComment(comment);
                req.setUpdatedAt(Instant.now());

                Request saved = requestRepository.save(req);

                if (nextStatus == RequestStatus.WAITING_ADDITIONAL_INFO) {
                    createCitizenMessage(saved, employee,
                        RequestMessageType.REQUEST_ADDITIONAL_INFO,
                        comment
                    );
                } else if (nextStatus == RequestStatus.REJECTED) {
                    createCitizenMessage(saved, employee,
                        RequestMessageType.REJECTION_REASON,
                        comment
                    );
                }

                notifyCitizenOnStatusChange(saved);
                return requestMapper.convertRequestToView(saved);
            });
    }

    private boolean requiresComment(RequestStatus nextStatus) {
        return nextStatus == RequestStatus.WAITING_ADDITIONAL_INFO
            || nextStatus == RequestStatus.REJECTED;
    }

    private void createCitizenMessage(Request request, Person employee, RequestMessageType type, String message) {
        RequestMessage m = new RequestMessage();
        m.setRequest(request);
        m.setType(type);
        m.setVisibleToCitizen(true);
        m.setMessage(message);

        String createdBy = "Employee: " + employee.getLastName() + " " + employee.getFirstName();
        m.setCreatedBy(createdBy);

        requestMessageRepository.save(m);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RequestView> getMyRequestDetails(Long requestId, Person employee) {
        return requestRepository.findByIdAndAssignedEmployee(requestId, employee)
            .map(requestMapper::convertRequestToView);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestMessageView> getMyRequestMessages(Long requestId, Person employee) {
        Request req = requestRepository.findByIdAndAssignedEmployee(requestId, employee)
            .orElseThrow(() -> new IllegalStateException("Request not found"));

        return requestMessageRepository.findAllByRequestOrderByCreatedAtAsc(req)
            .stream()
            .map(requestMessageMapper::toView)
            .toList();
    }

    @Override
    @Transactional
    public void citizenSubmittedAdditionalInfo(Long requestId, Person citizen, int uploadedFilesCount, String note) {
        Request req = requestRepository.findByIdAndCitizen(requestId, citizen)
            .orElseThrow(() -> new IllegalArgumentException("REQUEST_NOT_FOUND"));

        if (req.getStatus() != RequestStatus.WAITING_ADDITIONAL_INFO) {
            throw new IllegalStateException("REQUEST_NOT_WAITING_ADDITIONAL_INFO");
        }

        String base = (uploadedFilesCount == 1)
            ? "Ο πολίτης ανέβασε 1 επιπλέον αρχείο."
            : "Ο πολίτης ανέβασε " + uploadedFilesCount + " επιπλέον αρχεία.";

        String msg = base;
        if (note != null && !note.trim().isEmpty()) {
            msg += "\n\nΣημείωση πολίτη: " + note.trim();
        }

        RequestMessage m = new RequestMessage();
        m.setRequest(req);
        m.setType(RequestMessageType.CITIZEN_ADDITIONAL_INFO);
        m.setVisibleToCitizen(true);
        m.setMessage(msg);

        String createdBy = "Citizen: " + citizen.getLastName() + " " + citizen.getFirstName();
        m.setCreatedBy(createdBy);

        requestMessageRepository.save(m);

        req.setUpdatedAt(Instant.now());
        requestRepository.save(req);
    }

    private String generateProtocolNumber() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void notifyCitizenOnStatusChange(Request request) {
        String phone = request.getCitizen().getMobilePhoneNumber();
        String protocol = request.getProtocolNumber();

        String msg = switch (request.getStatus()) {
            case IN_PROGRESS ->
                "MyCityGov: Το αίτημά σου (" + protocol + ") ξεκίνησε να επεξεργάζεται.";
            case WAITING_ADDITIONAL_INFO ->
                "MyCityGov: Απαιτούνται επιπλέον στοιχεία για το αίτημα (" + protocol + ").";
            case COMPLETED ->
                "MyCityGov: Το αίτημά σου (" + protocol + ") ολοκληρώθηκε επιτυχώς.";
            case REJECTED ->
                "MyCityGov: Το αίτημά σου (" + protocol + ") απορρίφθηκε.";
            default -> null;
        };

        if (msg != null && phone != null && !phone.isBlank()) {
            smsNotificationPort.sendSms(phone, msg);
        }
    }
}
