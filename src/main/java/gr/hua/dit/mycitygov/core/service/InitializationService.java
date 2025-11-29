package gr.hua.dit.mycitygov.core.service;

import gr.hua.dit.mycitygov.core.model.PersonRole;
import gr.hua.dit.mycitygov.core.service.model.CreatePersonRequest;
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

    /** Για να τρέχει μόνο μία φορά. */
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    public InitializationService(final PersonService personService) {
        if (personService == null) throw new NullPointerException("personService");
        this.personService = personService;
    }

    @PostConstruct
    public void initialize() {
        if (!initialized.compareAndSet(false, true)) {
            // έχει ήδη τρέξει
            return;
        }

        LOGGER.info("Starting MyCityGov database initialization…");

        final List<CreatePersonRequest> users = List.of(
            // Citizen
            new CreatePersonRequest(
                PersonRole.CITIZEN,
                "citizen1@mycity.gov",
                "Giorgos",
                "Papadopoulos",
                "+306900000001",
                "111111111",
                "11111111111",
                "citizen1"
            ),
            // Admin
            new CreatePersonRequest(
                PersonRole.ADMIN,
                "admin@mycity.gov",
                "Admin",
                "User",
                "+306900000000",
                "999999999",
                "99999999999",
                "admin"
            ),
            // Employee
            new CreatePersonRequest(
                PersonRole.EMPLOYEE,
                "employee1@mycity.gov",
                "Maria",
                "Papadopoulou",
                "+306900000002",
                "222222222",
                "22222222222",
                "emp1"
            )
        );

        users.forEach(personService::createPerson);

        LOGGER.info("Database initialization completed successfully.");
    }
}
