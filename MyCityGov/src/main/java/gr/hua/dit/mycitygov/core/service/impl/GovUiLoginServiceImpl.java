package gr.hua.dit.mycitygov.core.service.impl;

import gr.hua.dit.mycitygov.core.model.Person;
import gr.hua.dit.mycitygov.core.model.PersonRole;
import gr.hua.dit.mycitygov.core.port.MockGovPort;
import gr.hua.dit.mycitygov.core.port.impl.dto.CitizenIdentityDto;
import gr.hua.dit.mycitygov.core.repository.PersonRepository;
import gr.hua.dit.mycitygov.core.service.GovUiLoginService;
import gr.hua.dit.mycitygov.core.service.PersonService;
import gr.hua.dit.mycitygov.core.service.model.CreatePersonRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GovUiLoginServiceImpl implements GovUiLoginService {

    private final MockGovPort mockGovPort;
    private final PersonRepository personRepository;
    private final PersonService personService;

    public GovUiLoginServiceImpl(MockGovPort mockGovPort,
                                 PersonRepository personRepository,
                                 PersonService personService) {
        this.mockGovPort = mockGovPort;
        this.personRepository = personRepository;
        this.personService = personService;
    }

    @Override
    public void pingProvider() {
        // GET προς MockGov για status/health
        mockGovPort.status();
    }

    @Override
    @Transactional
    public Person loginOrRegisterCitizenByToken(String userToken) {
        // Gov login μέσω token: validate στο external service και μετά login/registration στο MyCityGov
        if (userToken == null || userToken.isBlank()) {
            throw new IllegalArgumentException("Το gov token είναι κενό.");
        }

        CitizenIdentityDto dto = mockGovPort.validateUserToken(userToken.trim());

        Person existing = personRepository.findByAfm(dto.afm())
            .or(() -> personRepository.findByAmka(dto.amka()))
            .orElse(null);

        if (existing != null) {
            if (existing.getRole() != PersonRole.CITIZEN) {
                throw new IllegalArgumentException("Ο χρήστης υπάρχει αλλά δεν είναι πολίτης.");
            }
            return existing;
        }

        // Auto-create πολίτη αν δεν υπάρχει
        String email = buildUniqueEmail(dto.amka());
        String mobile = buildUniqueMobile(dto.afm());
        String rawPassword = "GovExt-" + dto.amka();

        CreatePersonRequest req = new CreatePersonRequest(
            PersonRole.CITIZEN,
            email,
            dto.firstName(),
            dto.lastName(),
            mobile,
            dto.afm(),
            dto.amka(),
            rawPassword
        );

        personService.createPerson(req, false);

        return personRepository.findByAfm(dto.afm())
            .or(() -> personRepository.findByAmka(dto.amka()))
            .orElseThrow(() -> new IllegalStateException("Αποτυχία δημιουργίας πολίτη από gov login."));
    }

    @Override
    @Transactional
    public Person loginOrRegisterCitizen(String afm, String amka, String lastName) {
        // Legacy flow: issue token + validate token
        String userToken = mockGovPort.issueUserToken(afm, amka, lastName);
        return loginOrRegisterCitizenByToken(userToken);
    }

    private String buildUniqueEmail(String amka) {
        // Φτιάχνει “μοναδικό” email για gov-created accounts
        String base = amka + "@gov.mock";
        if (!personRepository.existsByEmailAddressIgnoreCase(base)) return base;

        for (int i = 1; i < 1000; i++) {
            String alt = amka + "+" + i + "@gov.mock";
            if (!personRepository.existsByEmailAddressIgnoreCase(alt)) return alt;
        }
        throw new IllegalStateException("Δεν βρέθηκε μοναδικό email.");
    }

    private String buildUniqueMobile(String afm) {
        // Φτιάχνει “μοναδικό” κινητό (dummy) για gov-created accounts
        String base = "69" + last8Digits(afm);
        if (!personRepository.existsByMobilePhoneNumber(base)) return base;

        for (int i = 1; i <= 5000; i++) {
            String alt = "69" + String.format("%08d", i);
            if (!personRepository.existsByMobilePhoneNumber(alt)) return alt;
        }
        throw new IllegalStateException("Δεν βρέθηκε μοναδικό κινητό.");
    }

    private String last8Digits(String s) {
        String digits = (s == null) ? "" : s.replaceAll("\\D", "");
        if (digits.length() < 8) digits = ("00000000" + digits).substring(digits.length());
        return digits.substring(digits.length() - 8);
    }
}
