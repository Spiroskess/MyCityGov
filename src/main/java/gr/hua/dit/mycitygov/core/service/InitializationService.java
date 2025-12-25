package gr.hua.dit.mycitygov.core.service;

import gr.hua.dit.mycitygov.core.model.Person;
import gr.hua.dit.mycitygov.core.model.PersonRole;
import gr.hua.dit.mycitygov.core.repository.PersonRepository;
import gr.hua.dit.mycitygov.core.service.model.CreatePersonRequest;
import gr.hua.dit.mycitygov.core.service.model.MunicipalService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class InitializationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitializationService.class);

    private final PersonService personService;
    private final PersonRepository personRepository;

    /** Για να τρέχει μόνο μία φορά. */
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    public InitializationService(final PersonService personService,
                                 final PersonRepository personRepository) {
        if (personService == null) throw new NullPointerException("personService");
        if (personRepository == null) throw new NullPointerException("personRepository");
        this.personService = personService;
        this.personRepository = personRepository;
    }

    @PostConstruct
    public void initialize() {
        if (!initialized.compareAndSet(false, true)) {
            return;
        }

        LOGGER.info("Starting MyCityGov initialization (seed users)…");

        final List<SeedUser> users = List.of(
            // ADMIN (χωρίς υπηρεσία)
            new SeedUser(
                new CreatePersonRequest(
                    PersonRole.ADMIN,
                    "admin@mycity.gov",
                    "Artemakis",
                    "Papadopoulos",
                    "+306900000000",
                    "999999999",
                    "99999999999",
                    "Admin1"
                ),
                null
            ),

            // EMPLOYEE -> ΚΕΠ
            new SeedUser(
                new CreatePersonRequest(
                    PersonRole.EMPLOYEE,
                    "employee1@mycity.gov",
                    "Soula",
                    "Koromila",
                    "+306900000001",
                    "111111111",
                    "11111111111",
                    "Emp1"
                ),
                MunicipalService.KEP
            ),

            // EMPLOYEE -> Τεχνική Υπηρεσία
            new SeedUser(
                new CreatePersonRequest(
                    PersonRole.EMPLOYEE,
                    "employee2@mycity.gov",
                    "Maria",
                    "Papadopoulou",
                    "+306900000002",
                    "222222222",
                    "22222222222",
                    "Emp2"
                ),
                MunicipalService.TECHNICAL_SERVICE
            )
        );

        for (SeedUser seed : users) {


            final boolean sendSms = seed.request().role() == PersonRole.CITIZEN;


            if (personRepository.findByEmailAddressIgnoreCase(seed.request().emailAddress()).isPresent()) {
                LOGGER.info("Seed user already exists: {}", seed.request().emailAddress());

                if (seed.municipalService() != null) {
                    assignMunicipalService(seed.request().emailAddress(), seed.municipalService());
                }
                continue;
            }

            personService.createPerson(seed.request(), sendSms);

            if (seed.municipalService() != null) {
                assignMunicipalService(seed.request().emailAddress(), seed.municipalService());
            }
        }

        LOGGER.info("Initialization complete.");
    }

    private void assignMunicipalService(final String emailAddress, final MunicipalService municipalService) {
        final Person person = personRepository.findByEmailAddressIgnoreCase(emailAddress)
            .orElseThrow(() -> new IllegalStateException("Seed person not found: " + emailAddress));

        if (person.getMunicipalService() == municipalService) {
            return;
        }

        person.setMunicipalService(municipalService);
        personRepository.save(person);

        LOGGER.info("Assigned {} -> municipalService={}", emailAddress, municipalService);
    }

    private record SeedUser(CreatePersonRequest request, MunicipalService municipalService) {}
}
