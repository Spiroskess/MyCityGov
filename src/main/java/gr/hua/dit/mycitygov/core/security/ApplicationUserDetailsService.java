package gr.hua.dit.mycitygov.core.security;

import gr.hua.dit.mycitygov.core.model.Person;
import gr.hua.dit.mycitygov.core.repository.PersonRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementation of Spring's {@code UserDetailsService} for providing application users.
 */
@Service
public class ApplicationUserDetailsService implements UserDetailsService {

    private final PersonRepository personRepository;

    public ApplicationUserDetailsService(final PersonRepository personRepository) {
        if (personRepository == null) throw new NullPointerException("personRepository");
        this.personRepository = personRepository;
    }

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        if (username == null) throw new NullPointerException("username");
        if (username.isBlank()) throw new IllegalArgumentException("username is blank");

        final Person person = this.personRepository
            .findByEmailAddressIgnoreCase(username.strip())
            .orElse(null);

        if (person == null) {
            throw new UsernameNotFoundException("person with emailAddress " + username + " does not exist");
        }

        return new ApplicationUserDetails(
            person.getId(),                 // Long → autounbox σε long
            person.getEmailAddress(),
            person.getPasswordHash(),
            person.getRole()
        );
    }
}
