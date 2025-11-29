package gr.hua.dit.mycitygov.core.service.impl;

import gr.hua.dit.mycitygov.core.model.Person;
import gr.hua.dit.mycitygov.core.model.PersonRole;
import gr.hua.dit.mycitygov.core.repository.PersonRepository;
import gr.hua.dit.mycitygov.core.service.PersonService;
import gr.hua.dit.mycitygov.core.service.mapper.PersonMapper;
import gr.hua.dit.mycitygov.core.service.model.CreatePersonRequest;
import gr.hua.dit.mycitygov.core.service.model.CreatePersonResult;
import gr.hua.dit.mycitygov.core.service.model.PersonView;

import jakarta.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PersonServiceImpl implements PersonService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersonServiceImpl.class);

    private final Validator validator;
    private final PasswordEncoder passwordEncoder;
    private final PersonRepository personRepository;
    private final PersonMapper personMapper;

    public PersonServiceImpl(
        final Validator validator,
        final PasswordEncoder passwordEncoder,
        final PersonRepository personRepository,
        final PersonMapper personMapper
    ) {
        this.validator = validator;
        this.passwordEncoder = passwordEncoder;
        this.personRepository = personRepository;
        this.personMapper = personMapper;
    }

    @Override
    @Transactional
    public CreatePersonResult createPerson(final CreatePersonRequest request, final boolean notify) {

        if (request == null) {
            throw new NullPointerException("request");
        }

        // Ρόλος: αν είναι null (μόνο από τη φόρμα εγγραφής), τον κάνουμε default CITIZEN.
        // Για τα seed δεδομένα (InitializationService) έρχεται σωστός ρόλος (ADMIN / EMPLOYEE κτλ).
        PersonRole role = request.role() != null
            ? request.role()
            : PersonRole.CITIZEN;

        // Έλεγχοι μοναδικότητας
        if (personRepository.existsByEmailAddressIgnoreCase(request.emailAddress())) {
            return CreatePersonResult.fail("Υπάρχει ήδη χρήστης με αυτό το email.");
        }
        if (personRepository.existsByAfm(request.afm())) {
            return CreatePersonResult.fail("Υπάρχει ήδη χρήστης με αυτό το ΑΦΜ.");
        }
        if (personRepository.existsByAmka(request.amka())) {
            return CreatePersonResult.fail("Υπάρχει ήδη χρήστης με αυτό το ΑΜΚΑ.");
        }

        // Δημιουργία entity
        Person person = new Person();
        person.setRole(role); // 👈 εδώ μπαίνει ο ΠΡΑΓΜΑΤΙΚΟΣ ρόλος
        person.setEmailAddress(request.emailAddress());
        person.setFirstName(request.firstName());
        person.setLastName(request.lastName());
        person.setMobilePhoneNumber(request.mobilePhoneNumber());
        person.setAfm(request.afm());
        person.setAmka(request.amka());
        person.setPasswordHash(passwordEncoder.encode(request.rawPassword()));

        // Αποθήκευση
        person = personRepository.save(person);
        PersonView view = personMapper.convertPersonToPersonView(person);

        LOGGER.info("Created person with id={} role={}", person.getId(), person.getRole());
        return CreatePersonResult.success(view);
    }
}
