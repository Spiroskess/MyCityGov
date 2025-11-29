package gr.hua.dit.mycitygov.web.ui.model;

import jakarta.validation.constraints.NotNull;

public class AssignRequestForm {

    @NotNull
    private Long employeeId;

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }
}
