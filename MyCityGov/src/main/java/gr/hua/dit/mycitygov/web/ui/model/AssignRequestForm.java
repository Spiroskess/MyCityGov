package gr.hua.dit.mycitygov.web.ui.model;

import gr.hua.dit.mycitygov.core.service.model.MunicipalService;
import jakarta.validation.constraints.NotNull;

public class AssignRequestForm {

    // ο admin επιλέγει MunicipalService ώστε να αναθέσει/δρομολογήσει ένα αίτημα
    @NotNull
    private MunicipalService service;

    public MunicipalService getService() {
        return service;
    }

    public void setService(MunicipalService service) {
        this.service = service;
    }
}
