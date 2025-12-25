package gr.hua.dit.mycitygov.core.service.impl;

import gr.hua.dit.mycitygov.core.model.Person;
import gr.hua.dit.mycitygov.core.model.PersonRole;
import gr.hua.dit.mycitygov.core.model.Request;
import gr.hua.dit.mycitygov.core.model.RequestStatus;
import gr.hua.dit.mycitygov.core.model.RequestType;
import gr.hua.dit.mycitygov.core.port.SmsNotificationPort;
import gr.hua.dit.mycitygov.core.repository.RequestRepository;
import gr.hua.dit.mycitygov.core.service.RequestService;
import gr.hua.dit.mycitygov.core.service.RequestStatusTransitions;
import gr.hua.dit.mycitygov.core.service.mapper.RequestMapper;
import gr.hua.dit.mycitygov.core.service.model.MunicipalService;
import gr.hua.dit.mycitygov.core.service.model.OpenRequestRequest;
import gr.hua.dit.mycitygov.core.service.model.RequestView;
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
    private final RequestMapper requestMapper;
    private final SmsNotificationPort smsNotificationPort;

    public RequestServiceImpl(
        RequestRepository requestRepository,
        RequestMapper requestMapper,
        SmsNotificationPort smsNotificationPort
    ) {
        this.requestRepository = requestRepository;
        this.requestMapper = requestMapper;
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

        LocalDate slaDueDate = calculateSlaDueDate(openReq.type());
        request.setSlaDueDate(slaDueDate);

        request = requestRepository.save(request);
        return requestMapper.convertRequestToView(request);
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
    public Optional<RequestView> assignRequestToService(Long requestId, MunicipalService service) {
        return requestRepository.findById(requestId)
            .map(request -> {
                request.setAssignedService(service);

                // Μόλις ανατεθεί σε υπηρεσία, μπαίνει σε "RECEIVED" (στο τμήμα)
                if (request.getStatus() == RequestStatus.SUBMITTED) {
                    request.setStatus(RequestStatus.RECEIVED);
                }

                request.setUpdatedAt(Instant.now());
                return requestMapper.convertRequestToView(request);
            });
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestView> getServiceQueue(MunicipalService service) {
        return requestRepository
            .findAllByAssignedServiceAndAssignedEmployeeIsNullOrderByCreatedAtDesc(service)
            .stream()
            .map(requestMapper::convertRequestToView)
            .toList();
    }

    @Override
    @Transactional
    public Optional<RequestView> claimRequest(Long requestId, Person employee) {
        if (employee.getRole() != PersonRole.EMPLOYEE) {
            return Optional.empty();
        }
        if (employee.getMunicipalService() == null) {
            return Optional.empty();
        }

        return requestRepository.findById(requestId)
            .filter(req -> req.getAssignedService() != null)
            .filter(req -> req.getAssignedService() == employee.getMunicipalService())
            .filter(req -> req.getAssignedEmployee() == null)
            .map(req -> {
                req.setAssignedEmployee(employee);
                req.setUpdatedAt(Instant.now());
                return requestMapper.convertRequestToView(req);
            });
    }

    @Override
    @Transactional
    public Optional<RequestView> updateStatus(Long requestId, Person employee, RequestStatus nextStatus, String comment) {
        return requestRepository.findById(requestId)
            .filter(req -> req.getAssignedEmployee() != null && req.getAssignedEmployee().getId().equals(employee.getId()))
            .filter(req -> RequestStatusTransitions.canMove(req.getStatus(), nextStatus))
            .map(req -> {
                req.setStatus(nextStatus);
                req.setStatusComment(comment);
                req.setUpdatedAt(Instant.now());

                notifyCitizenOnStatusChange(req);

                return requestMapper.convertRequestToView(req);
            });
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
                "MyCityGov: Το αίτημά σου (" + protocol + ") απορρίφθηκε. "
                    + (request.getStatusComment() != null ? request.getStatusComment() : "");
            default -> null;
        };

        if (msg != null) {
            smsNotificationPort.sendSms(phone, msg);
        }
    }
}
