package gr.hua.dit.mycitygov.core.service.impl;

import gr.hua.dit.mycitygov.core.model.Person;
import gr.hua.dit.mycitygov.core.model.PersonRole;
import gr.hua.dit.mycitygov.core.port.PhoneNumberPort;
import gr.hua.dit.mycitygov.core.port.SmsNotificationPort;
import gr.hua.dit.mycitygov.core.port.impl.dto.PhoneNumberValidationResult;
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

    // NOC ports
    private final PhoneNumberPort phoneNumberPort;
    private final SmsNotificationPort smsNotificationPort;

    public PersonServiceImpl(
        final Validator validator,
        final PasswordEncoder passwordEncoder,
        final PersonRepository personRepository,
        final PersonMapper personMapper,
        final PhoneNumberPort phoneNumberPort,
        final SmsNotificationPort smsNotificationPort
    ) {
        this.validator = validator;
        this.passwordEncoder = passwordEncoder;
        this.personRepository = personRepository;
        this.personMapper = personMapper;
        this.phoneNumberPort = phoneNumberPort;
        this.smsNotificationPort = smsNotificationPort;
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

        // 1) Validate + normalize phone with NOC (E164)
        PhoneNumberValidationResult validation = phoneNumberPort.validate(request.mobilePhoneNumber());
        if (validation == null || !validation.isValidMobile()) {
            return CreatePersonResult.fail("Μη έγκυρο κινητό (πρέπει να είναι mobile).");
        }
        final String e164 = validation.e164();

        // Δημιουργία entity
        Person person = new Person();
        person.setRole(role);
        person.setEmailAddress(request.emailAddress());
        person.setFirstName(request.firstName());
        person.setLastName(request.lastName());
        person.setMobilePhoneNumber(e164); // αποθήκευση normalized E164
        person.setAfm(request.afm());
        person.setAmka(request.amka());
        person.setPasswordHash(passwordEncoder.encode(request.rawPassword()));

        // Αποθήκευση
        person = personRepository.save(person);
        PersonView view = personMapper.convertPersonToPersonView(person);

        // 2) SMS success (optional)
        if (notify) {
            String msg = "MyCityGov: Καλώς ήρθες " + person.getFirstName()
                + "! Η εγγραφή σου ολοκληρώθηκε επιτυχώς.";
            smsNotificationPort.sendSms(person.getMobilePhoneNumber(), msg);
        }

        LOGGER.info("Created person with id={} role={}", person.getId(), person.getRole());
        return CreatePersonResult.success(view);
    }
}
