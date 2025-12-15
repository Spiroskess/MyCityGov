package gr.hua.dit.mycitygov.core.service.impl;

import gr.hua.dit.mycitygov.core.model.RequestType;
import gr.hua.dit.mycitygov.core.model.Person;
import gr.hua.dit.mycitygov.core.model.Request;
import gr.hua.dit.mycitygov.core.model.RequestStatus;
import gr.hua.dit.mycitygov.core.port.SmsNotificationPort;
import gr.hua.dit.mycitygov.core.repository.RequestRepository;
import gr.hua.dit.mycitygov.core.service.RequestService;
import gr.hua.dit.mycitygov.core.service.RequestStatusTransitions;
import gr.hua.dit.mycitygov.core.service.mapper.RequestMapper;
import gr.hua.dit.mycitygov.core.service.model.OpenRequestRequest;
import gr.hua.dit.mycitygov.core.service.model.RequestView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Instant;
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

        //  υπολογισμός προθεσμίας (SLA) ανά τύπο αιτήματος
        LocalDate slaDueDate = calculateSlaDueDate(openReq.type());
        request.setSlaDueDate(slaDueDate);

        request = requestRepository.save(request);
        return requestMapper.convertRequestToView(request);
    }

    private LocalDate calculateSlaDueDate(RequestType type) {
        LocalDate today = LocalDate.now();

        return switch (type) {
            case CERTIFICATE_RESIDENCE -> today.plusDays(10);   // 10 μέρες
            case SIDEWALK_LICENSE      -> today.plusDays(15);   // 15 μέρες
            case LIGHTING_ISSUE,
                 ROAD_HOLE,
                 CLEANING_ISSUE        -> today.plusDays(5);    // βλάβες πόλης: πιο γρήγορα
            case OTHER                 -> today.plusDays(20);   // γενικά αιτήματα
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
    public Optional<RequestView> assignRequestToEmployee(Long requestId, Person employee) {
        return requestRepository.findById(requestId)
            .map(request -> {
                request.setAssignedEmployee(employee);
                request.setStatus(RequestStatus.RECEIVED);
                request.setUpdatedAt(Instant.now());
                return requestMapper.convertRequestToView(request);
            });
    }

    /*@Override
    public Optional<RequestView> updateStatus(Long requestId, Person employee, RequestStatus nextStatus, String comment) {
        return Optional.empty();
    }*/



    private String generateProtocolNumber() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @Override
    @Transactional
    public Optional<RequestView> updateStatus(
        Long requestId,
        Person employee,
        RequestStatus nextStatus,
        String comment
    ) {
        return requestRepository.findById(requestId).map(request -> {

            if (request.getAssignedEmployee() == null ||
                !request.getAssignedEmployee().getId().equals(employee.getId())) {
                throw new IllegalStateException("Δεν έχεις δικαίωμα για αυτό το αίτημα");
            }

            if (!RequestStatusTransitions.canMove(request.getStatus(), nextStatus)) {
                throw new IllegalStateException(
                    "Μη επιτρεπτή μετάβαση: "
                        + request.getStatus() + " → " + nextStatus
                );
            }

            request.setStatus(nextStatus);
            request.setUpdatedAt(Instant.now());

            if (comment != null && !comment.isBlank()) {
                request.setStatusComment(comment.trim());
            }

            sendSmsIfNeeded(request);

            return requestMapper.convertRequestToView(request);
        });
    }
    private void sendSmsIfNeeded(Request request) {
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
