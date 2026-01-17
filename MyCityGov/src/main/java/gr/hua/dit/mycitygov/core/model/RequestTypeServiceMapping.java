package gr.hua.dit.mycitygov.core.model;

import gr.hua.dit.mycitygov.core.service.model.MunicipalService;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

/**
 * Κανόνας αντιστοίχισης: RequestType -> MunicipalService.
 * Ένας τύπος μπορεί να έχει 0 ή 1 προεπιλεγμένη υπηρεσία.
 */
@Entity
@Table(
    name = "request_type_service_mapping",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_request_type_service_mapping_type", columnNames = {"request_type_id"})
    }
)
public class RequestTypeServiceMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "request_type_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "fk_request_type_service_mapping_type")
    )
    private RequestTypeEntity requestType;

    @Enumerated(EnumType.STRING)
    @Column(name = "municipal_service", length = 64)
    private MunicipalService municipalService;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public RequestTypeEntity getRequestType() { return requestType; }
    public void setRequestType(RequestTypeEntity requestType) { this.requestType = requestType; }

    public MunicipalService getMunicipalService() { return municipalService; }
    public void setMunicipalService(MunicipalService municipalService) { this.municipalService = municipalService; }
}
