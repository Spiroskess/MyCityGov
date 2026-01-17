package gr.hua.dit.mycitygov.core.service.impl;

import gr.hua.dit.mycitygov.core.model.RequestTypeEntity;
import gr.hua.dit.mycitygov.core.model.RequestTypeServiceMapping;
import gr.hua.dit.mycitygov.core.repository.RequestTypeRepository;
import gr.hua.dit.mycitygov.core.repository.RequestTypeServiceMappingRepository;
import gr.hua.dit.mycitygov.core.service.RequestTypeService;
import gr.hua.dit.mycitygov.core.service.model.MunicipalService;
import gr.hua.dit.mycitygov.core.service.model.RequestTypeView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RequestTypeServiceImpl implements RequestTypeService {

    private final RequestTypeRepository requestTypeRepository;
    private final RequestTypeServiceMappingRepository mappingRepository;

    public RequestTypeServiceImpl(RequestTypeRepository requestTypeRepository,
                                  RequestTypeServiceMappingRepository mappingRepository) {
        this.requestTypeRepository = requestTypeRepository;
        this.mappingRepository = mappingRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestTypeView> listAll() {
        return requestTypeRepository.findAllByOrderByIdAsc()
            .stream()
            .map(this::toView)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RequestTypeView> listEnabled() {
        return requestTypeRepository.findAllByEnabledTrueOrderByIdAsc()
            .stream()
            .map(this::toView)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RequestTypeView> findByCode(String code) {
        if (code == null) return Optional.empty();
        return requestTypeRepository.findByCode(code).map(this::toView);
    }

    @Override
    @Transactional
    public RequestTypeView create(String code, String title, int slaDays, boolean enabled) {
        validate(code, title, slaDays);

        if (requestTypeRepository.existsByCode(code)) {
            throw new IllegalStateException("Request type code already exists: " + code);
        }

        RequestTypeEntity e = new RequestTypeEntity();
        e.setCode(code);
        e.setTitle(title);
        e.setSlaDays(slaDays);
        e.setEnabled(enabled);

        return toView(requestTypeRepository.save(e));
    }

    @Override
    @Transactional
    public RequestTypeView update(String code, String title, int slaDays, boolean enabled) {
        validate(code, title, slaDays);

        RequestTypeEntity e = requestTypeRepository.findByCode(code)
            .orElseThrow(() -> new IllegalArgumentException("UNKNOWN_REQUEST_TYPE"));

        e.setTitle(title);
        e.setSlaDays(slaDays);
        e.setEnabled(enabled);

        return toView(requestTypeRepository.save(e));
    }

    @Override
    @Transactional
    public void setEnabled(String code, boolean enabled) {
        RequestTypeEntity e = requestTypeRepository.findByCode(code)
            .orElseThrow(() -> new IllegalArgumentException("UNKNOWN_REQUEST_TYPE"));

        e.setEnabled(enabled);
        requestTypeRepository.save(e);
    }

    @Override
    @Transactional
    public void setDefaultService(String requestTypeCode, MunicipalService municipalService) {
        if (requestTypeCode == null || requestTypeCode.isBlank()) {
            throw new IllegalArgumentException("REQUEST_TYPE_CODE_REQUIRED");
        }

        RequestTypeEntity type = requestTypeRepository.findByCode(requestTypeCode)
            .orElseThrow(() -> new IllegalArgumentException("UNKNOWN_REQUEST_TYPE"));

        if (municipalService == null) {
            mappingRepository.deleteByRequestType_Code(requestTypeCode);
            return;
        }

        RequestTypeServiceMapping m = mappingRepository.findByRequestType_Code(requestTypeCode)
            .orElseGet(() -> {
                RequestTypeServiceMapping x = new RequestTypeServiceMapping();
                x.setRequestType(type);
                return x;
            });

        m.setMunicipalService(municipalService);
        mappingRepository.save(m);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MunicipalService> getDefaultService(String requestTypeCode) {
        if (requestTypeCode == null || requestTypeCode.isBlank()) return Optional.empty();
        return mappingRepository.findByRequestType_Code(requestTypeCode)
            .map(RequestTypeServiceMapping::getMunicipalService);
    }

    private RequestTypeView toView(RequestTypeEntity e) {
        MunicipalService mapped = mappingRepository.findByRequestType_Code(e.getCode())
            .map(RequestTypeServiceMapping::getMunicipalService)
            .orElse(null);

        return new RequestTypeView(
            e.getCode(),
            e.getTitle(),
            Boolean.TRUE.equals(e.getEnabled()),
            e.getSlaDays() == null ? 0 : e.getSlaDays(),
            mapped
        );
    }

    private void validate(String code, String title, int slaDays) {
        if (code == null || code.isBlank()) throw new IllegalArgumentException("CODE_REQUIRED");
        if (title == null || title.isBlank()) throw new IllegalArgumentException("TITLE_REQUIRED");
        if (slaDays < 1) throw new IllegalArgumentException("SLA_DAYS_MIN_1");
    }
}
