package gr.hua.dit.mycitygov.core.service.impl;

import gr.hua.dit.mycitygov.core.model.Citizen;
import gr.hua.dit.mycitygov.core.model.Request;
import gr.hua.dit.mycitygov.core.model.RequestStatus;
import gr.hua.dit.mycitygov.core.model.Role;
import gr.hua.dit.mycitygov.core.repository.CitizenRepository;
import gr.hua.dit.mycitygov.core.repository.RequestRepository;
import gr.hua.dit.mycitygov.core.service.EmployeeRequestService;
import gr.hua.dit.mycitygov.core.service.mapper.EmployeeRequestMapper;
import gr.hua.dit.mycitygov.core.service.model.EmployeeRequestView;
import gr.hua.dit.mycitygov.core.service.model.UpdateRequestStatusRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EmployeeRequestServiceImpl implements EmployeeRequestService {

    private final RequestRepository requestRepository;
    private final CitizenRepository citizenRepository;
    private final EmployeeRequestMapper employeeRequestMapper;

    public EmployeeRequestServiceImpl(RequestRepository requestRepository,
                                      CitizenRepository citizenRepository,
                                      EmployeeRequestMapper employeeRequestMapper) {
        this.requestRepository = requestRepository;
        this.citizenRepository = citizenRepository;
        this.employeeRequestMapper = employeeRequestMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeRequestView> getAllRequests() {
        return requestRepository.findAllByOrderBySubmittedAtDesc()
            .stream()
            .map(employeeRequestMapper::toView)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeRequestView> getAssignedRequests(String employeeEmail) {
        Citizen employee = findEmployeeByEmail(employeeEmail);
        return requestRepository.findByAssignedEmployeeOrderBySubmittedAtDesc(employee)
            .stream()
            .map(employeeRequestMapper::toView)
            .toList();
    }

    @Override
    public void assignToSelf(Long requestId, String employeeEmail) {
        Citizen employee = findEmployeeByEmail(employeeEmail);
        Request request = requestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        request.setAssignedEmployee(employee);

        // αν ήταν SUBMITTED, το περνάμε σε IN_REVIEW
        if (request.getStatus() == RequestStatus.SUBMITTED) {
            request.setStatus(RequestStatus.IN_REVIEW);
        }
    }

    @Override
    public void updateStatus(Long requestId, String employeeEmail, UpdateRequestStatusRequest dto) {
        Citizen employee = findEmployeeByEmail(employeeEmail);
        Request request = requestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        // αν δεν έχει ήδη assignedEmployee, τον βάζουμε αυτόματα
        if (request.getAssignedEmployee() == null) {
            request.setAssignedEmployee(employee);
        }

        request.setStatus(dto.getStatus());
        request.setEmployeeComment(dto.getEmployeeComment());
    }

    private Citizen findEmployeeByEmail(String email) {
        Citizen citizen = citizenRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        if (citizen.getRole() != Role.EMPLOYEE && citizen.getRole() != Role.ADMIN) {
            throw new IllegalStateException("User is not employee");
        }
        return citizen;
    }
}
