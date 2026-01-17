package gr.hua.dit.mycitygov.web.ui.model;

import gr.hua.dit.mycitygov.core.service.model.MunicipalService;

/** nullable municipalService => καθαρισμός αντιστοίχισης */
public class RequestTypeMappingForm {

    private MunicipalService municipalService;

    public MunicipalService getMunicipalService() { return municipalService; }
    public void setMunicipalService(MunicipalService municipalService) { this.municipalService = municipalService; }
}
