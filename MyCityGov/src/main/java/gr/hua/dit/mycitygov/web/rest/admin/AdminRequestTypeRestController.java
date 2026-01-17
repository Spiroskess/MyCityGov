package gr.hua.dit.mycitygov.web.rest.admin;

import gr.hua.dit.mycitygov.core.service.RequestTypeService;
import gr.hua.dit.mycitygov.core.service.model.MunicipalService;
import gr.hua.dit.mycitygov.core.service.model.RequestTypeView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/admin/request-types")
@Tag(name = "Admin - Request Types", description = "CRUD για τύπους αιτημάτων (DB-driven) + mapping τύπος -> υπηρεσία")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRequestTypeRestController {

    private final RequestTypeService requestTypeService;

    public AdminRequestTypeRestController(RequestTypeService requestTypeService) {
        this.requestTypeService = requestTypeService;
    }

    @GetMapping
    @Operation(summary = "Λίστα όλων των τύπων αιτημάτων")
    public List<RequestTypeView> listAll() {
        return requestTypeService.listAll();
    }

    @GetMapping("/enabled")
    @Operation(summary = "Λίστα μόνο των ενεργών τύπων αιτημάτων (enabled=true)")
    public List<RequestTypeView> listEnabled() {
        return requestTypeService.listEnabled();
    }

    @GetMapping("/{code}")
    @Operation(summary = "Λεπτομέρειες τύπου αιτήματος με βάση τον κωδικό")
    public RequestTypeView getOne(@PathVariable String code) {
        return requestTypeService.findByCode(code)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "REQUEST_TYPE_NOT_FOUND"));
    }

    @PostMapping
    @Operation(summary = "Δημιουργία νέου τύπου αιτήματος")
    public ResponseEntity<RequestTypeView> create(@Valid @RequestBody CreateRequest body) {
        try {
            RequestTypeView created = requestTypeService.create(
                body.code().trim(),
                body.title().trim(),
                body.slaDays(),
                body.enabled()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalStateException ex) {
            // π.χ. code already exists
            throw new ResponseStatusException(HttpStatus.CONFLICT, ex.getMessage(), ex);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    @PutMapping("/{code}")
    @Operation(summary = "Ενημέρωση υπάρχοντος τύπου αιτήματος")
    public RequestTypeView update(@PathVariable String code, @Valid @RequestBody UpdateRequest body) {
        try {
            return requestTypeService.update(
                code,
                body.title().trim(),
                body.slaDays(),
                body.enabled()
            );
        } catch (IllegalArgumentException ex) {
            if ("UNKNOWN_REQUEST_TYPE".equals(ex.getMessage())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "REQUEST_TYPE_NOT_FOUND", ex);
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    @PatchMapping("/{code}/enabled")
    @Operation(summary = "Ενεργοποίηση/απενεργοποίηση τύπου αιτήματος")
    public ResponseEntity<Void> setEnabled(@PathVariable String code, @Valid @RequestBody EnabledRequest body) {
        try {
            requestTypeService.setEnabled(code, body.enabled());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            if ("UNKNOWN_REQUEST_TYPE".equals(ex.getMessage())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "REQUEST_TYPE_NOT_FOUND", ex);
            }
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    @GetMapping("/{code}/mapping")
    @Operation(summary = "Προεπιλεγμένη υπηρεσία (mapping) για τον τύπο αιτήματος")
    public MappingResponse getMapping(@PathVariable String code) {
        // ensure type exists
        requestTypeService.findByCode(code)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "REQUEST_TYPE_NOT_FOUND"));

        MunicipalService svc = requestTypeService.getDefaultService(code).orElse(null);
        return new MappingResponse(code, svc);
    }

    @PutMapping("/{code}/mapping")
    @Operation(summary = "Ορισμός/καθαρισμός mapping τύπου αιτήματος -> υπηρεσία (null => remove mapping)")
    public ResponseEntity<Void> setMapping(@PathVariable String code, @RequestBody MappingRequest body) {
        // ensure type exists
        requestTypeService.findByCode(code)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "REQUEST_TYPE_NOT_FOUND"));

        try {
            // Αν body == null ή municipalService == null => remove mapping
            MunicipalService svc = (body == null) ? null : body.municipalService();
            requestTypeService.setDefaultService(code, svc);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    // ===== Request/Response models (nested records για εύκολο copy-paste) =====

    public record CreateRequest(
        @NotNull @NotBlank @Size(max = 64) String code,
        @NotNull @NotBlank @Size(max = 160) String title,
        @NotNull @Min(1) @Max(3650) Integer slaDays,
        @NotNull Boolean enabled
    ) {
        public Integer slaDays() { return slaDays == null ? 0 : slaDays; }
        public Boolean enabled() { return enabled != null && enabled; }
    }

    public record UpdateRequest(
        @NotNull @NotBlank @Size(max = 160) String title,
        @NotNull @Min(1) @Max(3650) Integer slaDays,
        @NotNull Boolean enabled
    ) {
        public Integer slaDays() { return slaDays == null ? 0 : slaDays; }
        public Boolean enabled() { return enabled != null && enabled; }
    }

    public record EnabledRequest(@NotNull Boolean enabled) {
        public Boolean enabled() { return enabled != null && enabled; }
    }

    public record MappingRequest(MunicipalService municipalService) { }

    public record MappingResponse(String requestTypeCode, MunicipalService municipalService) { }
}
