package gr.hua.dit.mycitygov.core.service.model;

import gr.hua.dit.mycitygov.core.model.RequestStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UpdateRequestStatusRequest {

    @NotNull
    private RequestStatus status;

    @Size(max = 2000)
    private String employeeComment;

    public RequestStatus getStatus() { return status; }

    public void setStatus(RequestStatus status) { this.status = status; }

    public String getEmployeeComment() { return employeeComment; }

    public void setEmployeeComment(String employeeComment) {
        this.employeeComment = employeeComment;
    }
}
