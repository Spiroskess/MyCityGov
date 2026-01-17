package gr.hua.dit.mycitygov.core.service;

import gr.hua.dit.mycitygov.core.service.model.MunicipalService;
import gr.hua.dit.mycitygov.core.service.model.RequestTypeView;

import java.util.List;
import java.util.Optional;

public interface RequestTypeService {

    List<RequestTypeView> listAll();

    List<RequestTypeView> listEnabled();

    Optional<RequestTypeView> findByCode(String code);

    RequestTypeView create(String code, String title, int slaDays, boolean enabled);

    RequestTypeView update(String code, String title, int slaDays, boolean enabled);

    void setEnabled(String code, boolean enabled);

    void setDefaultService(String requestTypeCode, MunicipalService municipalService);

    Optional<MunicipalService> getDefaultService(String requestTypeCode);
}
