package gr.hua.dit.mycitygov.web.ui.model;

import gr.hua.dit.mycitygov.core.service.model.MunicipalService;
import jakarta.validation.constraints.NotNull;

public class AssignRequestForm {

    @NotNull
    private MunicipalService service;

    public MunicipalService getService() {
        return service;
    }

    public void setService(MunicipalService service) {
        this.service = service;
    }
}
