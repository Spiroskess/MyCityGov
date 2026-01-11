package gr.hua.dit.mycitygov.core.repository;

import gr.hua.dit.mycitygov.core.model.Person;
import gr.hua.dit.mycitygov.core.model.PersonRole;
import gr.hua.dit.mycitygov.core.service.model.MunicipalService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {

    // Αναζήτηση χρήστη με βασικά μοναδικά πεδία (email/afm/amka)
    Optional<Person> findByEmailAddressIgnoreCase(String emailAddress);
    Optional<Person> findByAfm(String afm);
    Optional<Person> findByAmka(String amka);

    // Έλεγχοι uniqueness για registration/validation
    boolean existsByEmailAddressIgnoreCase(String emailAddress);
    boolean existsByAfm(String afm);
    boolean existsByAmka(String amka);
    boolean existsByMobilePhoneNumber(String mobilePhoneNumber);

    // Λίστες χρηστών ανά ρόλο (π.χ. όλοι οι EMPLOYEE)
    List<Person> findAllByRoleOrderByLastName(PersonRole role);

    // Λίστες υπαλλήλων ανά υπηρεσία (π.χ. EMPLOYEE στο ΚΕΠ)
    List<Person> findAllByRoleAndMunicipalServiceOrderByLastName(PersonRole role, MunicipalService municipalService);
}
