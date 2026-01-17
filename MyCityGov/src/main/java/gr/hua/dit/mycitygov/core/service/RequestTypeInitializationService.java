package gr.hua.dit.mycitygov.core.service;

import gr.hua.dit.mycitygov.core.repository.RequestTypeRepository;
import gr.hua.dit.mycitygov.core.service.model.MunicipalService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class RequestTypeInitializationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RequestTypeInitializationService.class);

    private final RequestTypeService requestTypeService;
    private final RequestTypeRepository requestTypeRepository;

    private final AtomicBoolean initialized = new AtomicBoolean(false);

    public RequestTypeInitializationService(RequestTypeService requestTypeService,
                                            RequestTypeRepository requestTypeRepository) {
        this.requestTypeService = requestTypeService;
        this.requestTypeRepository = requestTypeRepository;
    }

    @PostConstruct
    public void initialize() {

        if (!initialized.compareAndSet(false, true)) {
            return;
        }

        LOGGER.info("Starting RequestType initialization (seed request types)…");

        // Codes = τα παλιά enum names (RequestType.java), για συμβατότητα
        List<SeedType> types = List.of(
            new SeedType("CERTIFICATE_RESIDENCE", "Βεβαίωση μόνιμης κατοικίας", 5, true, MunicipalService.KEP),
            new SeedType("SIDEWALK_LICENSE", "Άδεια κατάληψης πεζοδρομίου", 10, true, MunicipalService.TECHNICAL_SERVICE),
            new SeedType("LIGHTING_ISSUE", "Αναφορά προβλήματος φωτισμού", 3, true, MunicipalService.TECHNICAL_SERVICE),
            new SeedType("ROAD_HOLE", "Αναφορά λακκούβας / οδοστρώματος", 7, true, MunicipalService.TECHNICAL_SERVICE),
            new SeedType("CLEANING_ISSUE", "Αναφορά καθαριότητας", 4, true, MunicipalService.ENVIRONMENT_SERVICE),
            new SeedType("OTHER", "Άλλο", 5, true, null)
        );

        for (SeedType t : types) {

            if (!requestTypeRepository.existsByCode(t.code())) {
                requestTypeService.create(t.code(), t.title(), t.slaDays(), t.enabled());
                LOGGER.info("Seeded request type: {}", t.code());
            } else {
                LOGGER.info("Request type already exists: {}", t.code());
            }

            // Προαιρετικό: seed mapping τύπος -> υπηρεσία (αν θες default routing)
            // (θα το κάνει overwrite μόνο αν δεν υπάρχει mapping - εδώ το αφήνουμε να το ορίσει ο admin μετά)
            if (t.defaultService() != null) {
                // βάζουμε mapping μόνο αν δεν υπάρχει ήδη
                requestTypeService.getDefaultService(t.code()).ifPresentOrElse(
                    s -> { /* already mapped, do nothing */ },
                    () -> requestTypeService.setDefaultService(t.code(), t.defaultService())
                );
            }
        }

        LOGGER.info("RequestType initialization complete.");
    }

    private record SeedType(String code, String title, int slaDays, boolean enabled, MunicipalService defaultService) {}
}
