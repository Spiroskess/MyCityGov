package gr.hua.dit.mycitygov.core.repository;

import gr.hua.dit.mycitygov.core.model.Person;
import gr.hua.dit.mycitygov.core.model.PersonRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {

    Optional<Person> findByEmailAddressIgnoreCase(String emailAddress);

    boolean existsByEmailAddressIgnoreCase(String emailAddress);

    boolean existsByAfm(String afm);

    boolean existsByAmka(String amka);

    boolean existsByMobilePhoneNumber(String mobilePhoneNumber);

    List<Person> findAllByRoleOrderByLastName(PersonRole role);
}
