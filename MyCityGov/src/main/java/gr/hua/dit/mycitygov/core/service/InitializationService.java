package gr.hua.dit.mycitygov.core.service;

import gr.hua.dit.mycitygov.core.model.Person;
import gr.hua.dit.mycitygov.core.model.PersonRole;
import gr.hua.dit.mycitygov.core.repository.PersonRepository;
import gr.hua.dit.mycitygov.core.service.model.CreatePersonRequest;
import gr.hua.dit.mycitygov.core.service.model.CreatePersonResult;
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

    // Seed service: τρέχει στο startup για να δημιουργήσει αρχικούς χρήστες (admin/employees)
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
        // Εκτελεί seeding μία φορά
        if (!initialized.compareAndSet(false, true)) {
            return;
        }

        LOGGER.info("Starting MyCityGov initialization (seed users)…");

        final List<SeedUser> users = List.of(
            // Default ADMIN account
            new SeedUser(
                new CreatePersonRequest(
                    PersonRole.ADMIN,
                    "admin@mycity.gov",
                    "Giorgos",
                    "Papadopoulos",
                    "+306900000000",
                    "999999999",
                    "99999999999",
                    "Admin1!234"
                ),
                null
            ),

            // Default EMPLOYEE accounts
            new SeedUser(
                new CreatePersonRequest(
                    PersonRole.EMPLOYEE,
                    "employee1@mycity.gov",
                    "Soula",
                    "Koromila",
                    "+306900000001",
                    "111111111",
                    "11111111111",
                    "Emp1!23456"
                ),
                MunicipalService.KEP
            ),

            new SeedUser(
                new CreatePersonRequest(
                    PersonRole.EMPLOYEE,
                    "employee2@mycity.gov",
                    "Maria",
                    "Papadopoulou",
                    "+306900000002",
                    "222222222",
                    "22222222222",
                    "Emp2!23456"
                ),
                MunicipalService.TECHNICAL_SERVICE
            ),

            new SeedUser(
                new CreatePersonRequest(
                    PersonRole.EMPLOYEE,
                    "employee3@mycity.gov",
                    "Giorgos",
                    "Nikolaou",
                    "+306900000003",
                    "333333333",
                    "33333333333",
                    "Emp3!23456"
                ),
                MunicipalService.SOCIAL_SERVICE
            ),

            new SeedUser(
                new CreatePersonRequest(
                    PersonRole.EMPLOYEE,
                    "employee4@mycity.gov",
                    "Kostas",
                    "Oikonomou",
                    "+306900000004",
                    "444444444",
                    "44444444444",
                    "Emp4!23456"
                ),
                MunicipalService.FINANCIAL_SERVICE
            ),

            new SeedUser(
                new CreatePersonRequest(
                    PersonRole.EMPLOYEE,
                    "employee5@mycity.gov",
                    "Eleni",
                    "Menegaki",
                    "+306900000005",
                    "555555555",
                    "55555555555",
                    "Emp5!23456"
                ),
                MunicipalService.ENVIRONMENT_SERVICE
            )

        );

        for (SeedUser seed : users) {

            final boolean sendSms = seed.request().role() == PersonRole.CITIZEN;

            // Αν υπάρχει ήδη, δεν ξαναδημιουργείται
            if (personRepository.findByEmailAddressIgnoreCase(seed.request().emailAddress()).isPresent()) {
                LOGGER.info("Seed user already exists: {}", seed.request().emailAddress());

                if (seed.municipalService() != null) {
                    assignMunicipalService(seed.request().emailAddress(), seed.municipalService());
                }
                continue;
            }

            // Δημιουργία μέσω PersonService
            final CreatePersonResult result = personService.createPerson(seed.request(), sendSms);

            if (!result.created()) {
                LOGGER.warn("Seed user creation failed for {}: {}",
                    seed.request().emailAddress(),
                    result.reason()
                );
                continue;
            }

            if (seed.municipalService() != null) {
                assignMunicipalService(seed.request().emailAddress(), seed.municipalService());
            }
        }

        LOGGER.info("Initialization complete.");
    }

    private void assignMunicipalService(final String emailAddress, final MunicipalService municipalService) {
        // Helper assigns municipal service σε υπάρχον seed employee
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
