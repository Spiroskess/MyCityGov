package gr.hua.dit.mycitygov.core.service.model;

/** DTO για τύπους αιτημάτων (για UI/REST). */
public record RequestTypeView(
    String code,
    String title,
    boolean enabled,
    int slaDays,
    MunicipalService defaultService
) {}
