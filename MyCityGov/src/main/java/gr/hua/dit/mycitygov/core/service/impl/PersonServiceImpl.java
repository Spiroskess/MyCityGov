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

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class PersonServiceImpl implements PersonService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PersonServiceImpl.class);

    private final Validator validator;
    private final PasswordEncoder passwordEncoder;
    private final PersonRepository personRepository;
    private final PersonMapper personMapper;

    // External integrations (NOC) για validation τηλεφώνου και SMS notifications
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

        // Manual validation
        final Set<ConstraintViolation<CreatePersonRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            return CreatePersonResult.fail(violations.iterator().next().getMessage());
        }

        // Default ρόλος αν δεν δοθεί (CITIZEN)
        PersonRole role = request.role() != null ? request.role() : PersonRole.CITIZEN;

        // Uniqueness checks (email/AFM/AMKA)
        if (personRepository.existsByEmailAddressIgnoreCase(request.emailAddress())) {
            return CreatePersonResult.fail("Υπάρχει ήδη χρήστης με αυτό το email.");
        }
        if (personRepository.existsByAfm(request.afm())) {
            return CreatePersonResult.fail("Υπάρχει ήδη χρήστης με αυτό το ΑΦΜ.");
        }
        if (personRepository.existsByAmka(request.amka())) {
            return CreatePersonResult.fail("Υπάρχει ήδη χρήστης με αυτό το ΑΜΚΑ.");
        }

        // Validation τηλεφώνου μέσω external NOC service + αποθήκευση σε e164 μορφή
        PhoneNumberValidationResult validation = phoneNumberPort.validate(request.mobilePhoneNumber());
        if (validation == null || !validation.isValidMobile()) {
            return CreatePersonResult.fail("Μη έγκυρο κινητό (πρέπει να είναι mobile).");
        }
        final String e164 = validation.e164();

        Person person = new Person();
        person.setRole(role);
        person.setEmailAddress(request.emailAddress());
        person.setFirstName(request.firstName());
        person.setLastName(request.lastName());
        person.setMobilePhoneNumber(e164);
        person.setAfm(request.afm());
        person.setAmka(request.amka());
        person.setPasswordHash(passwordEncoder.encode(request.rawPassword())); // αποθήκευση hash όχι plain

        person = personRepository.save(person);
        PersonView view = personMapper.convertPersonToPersonView(person);

        if (notify) {
            // Προαιρετική αποστολή SMS μέσω external NOC service
            String msg = "MyCityGov: Καλώς ήρθες " + person.getFirstName()
                + "! Η εγγραφή σου ολοκληρώθηκε επιτυχώς.";
            smsNotificationPort.sendSms(person.getMobilePhoneNumber(), msg);
        }

        LOGGER.info("Created person with id={} role={}", person.getId(), person.getRole());
        return CreatePersonResult.success(view);
    }
}
