package gr.hua.dit.mycitygov.govauth;

import gr.hua.dit.mycitygov.core.model.Person;
import gr.hua.dit.mycitygov.core.model.PersonRole;
import gr.hua.dit.mycitygov.core.repository.PersonRepository;
import gr.hua.dit.mycitygov.core.service.PersonService;
import gr.hua.dit.mycitygov.core.service.model.CreatePersonRequest;
import gr.hua.dit.mycitygov.core.service.model.CreatePersonResult;
import gr.hua.dit.mycitygov.mockgov.dto.CitizenIdentityDto;
import org.springframework.stereotype.Service;

@Service
public class GovLoginService {

    private final GovAuthClient govAuthClient;
    private final PersonRepository personRepository;
    private final PersonService personService;

    public GovLoginService(GovAuthClient govAuthClient,
                           PersonRepository personRepository,
                           PersonService personService) {
        this.govAuthClient = govAuthClient;
        this.personRepository = personRepository;
        this.personService = personService;
    }

    /**
     * 1) Validates token μέσω external service
     * 2) Αν υπάρχει ήδη Person (CITIZEN) με ίδιο ΑΦΜ ή ΑΜΚΑ -> τον επιστρέφει
     * 3) Αλλιώς δημιουργεί νέο CITIZEN με PersonService.createPerson(...)
     */
    public Person authenticateAndUpsertCitizen(String token) {

        CitizenIdentityDto dto = govAuthClient.validateToken(token);

        // Αν υπάρχει ήδη -> επιστροφή
        return personRepository.findByAfm(dto.afm())
            .or(() -> personRepository.findByAmka(dto.amka()))
            .orElseGet(() -> createCitizenFromGov(dto));
    }

    private Person createCitizenFromGov(CitizenIdentityDto dto) {

        // Για να περάσουν τα constraints της εγγραφής, δημιουργούμε "mock" email/phone/password.
        // (Η εργασία δεν μας δίνει email/phone/password από gov.)
        String email = dto.amka() + "@gov.mock";

        // Προσοχή: το PersonService κάνει validate κινητό μέσω PhoneNumberPort.
        // Δίνουμε κάτι που μοιάζει με ελληνικό κινητό (10 ψηφία, ξεκινά 69...).
        String mobile = "69" + last8Digits(dto.afm());

        // Raw password (θα γίνει hash μέσα στο PersonService)
        String rawPassword = "Gov-" + dto.amka(); // απλό, για demo

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

        // Καλούμε το υπάρχον create flow.
        // Δεν βασιζόμαστε στο API του CreatePersonResult (success/message), γιατί στο project σου είναι διαφορετικό.
        CreatePersonResult ignored = personService.createPerson(req, false);

        // Είτε δημιουργήθηκε, είτε υπήρχε ήδη (duplicate) -> σε κάθε περίπτωση κάνουμε lookup και επιστρέφουμε entity.
        return personRepository.findByAfm(dto.afm())
            .or(() -> personRepository.findByAmka(dto.amka()))
            .orElseThrow(() -> new GovAuthException(
                "Αποτυχία δημιουργίας/εύρεσης πολίτη μετά από gov login."
            ));
    }

    private String last8Digits(String afm) {
        String digits = afm == null ? "" : afm.replaceAll("\\D", "");
        if (digits.length() < 8) {
            digits = ("00000000" + digits).substring(digits.length());
        }
        return digits.substring(digits.length() - 8);
    }
}
