package gr.hua.dit.mycitygov.core.security;

import gr.hua.dit.mycitygov.core.model.Person;
import gr.hua.dit.mycitygov.core.model.PersonRole;
import gr.hua.dit.mycitygov.core.repository.PersonRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Component for providing the current user (works for BOTH UI session auth and REST JWT auth).
 *
 * UI session principal: {@link ApplicationUserDetails}
 * REST JWT principal: {@link org.springframework.security.core.userdetails.User}
 */
@Component
public final class CurrentUserProvider {

    private final PersonRepository personRepository;

    public CurrentUserProvider(final PersonRepository personRepository) {
        if (personRepository == null) throw new NullPointerException("personRepository");
        this.personRepository = personRepository;
    }

    public Optional<CurrentUser> getCurrentUser() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        // UI (session/cookie auth)
        if (authentication.getPrincipal() instanceof ApplicationUserDetails userDetails) {
            return Optional.of(new CurrentUser(
                userDetails.personId(),
                userDetails.getUsername(),
                userDetails.role()
            ));
        }

        // REST (JWT auth) -> principal is usually spring-security User
        if (authentication.getPrincipal() instanceof User springUser) {
            final String email = springUser.getUsername();
            if (email == null || email.isBlank()) return Optional.empty();

            final Person person = personRepository.findByEmailAddressIgnoreCase(email)
                .orElseThrow(() -> new SecurityException("User not found by email: " + email));

            return Optional.of(new CurrentUser(
                person.getId(),
                person.getEmailAddress(),
                person.getRole()
            ));
        }

        // Fallback: try auth.getName() as email
        final String name = authentication.getName();
        if (name == null || name.isBlank() || "anonymousUser".equalsIgnoreCase(name)) {
            return Optional.empty();
        }

        final Person person = personRepository.findByEmailAddressIgnoreCase(name)
            .orElseThrow(() -> new SecurityException("User not found by email: " + name));

        return Optional.of(new CurrentUser(
            person.getId(),
            person.getEmailAddress(),
            person.getRole()
        ));
    }

    public CurrentUser requireCurrentUser() {
        return this.getCurrentUser()
            .orElseThrow(() -> new SecurityException("not authenticated"));
    }

    public Optional<Person> getCurrentPerson() {
        return getCurrentUser()
            .flatMap(u -> personRepository.findById(u.id()));
    }

    public Person requireCurrentPerson() {
        return getCurrentPerson()
            .orElseThrow(() -> new SecurityException("current person not found"));
    }

    public long requireCitizenId() {
        final var currentUser = this.requireCurrentUser();
        if (currentUser.role() != PersonRole.CITIZEN) {
            throw new SecurityException("Citizen role required");
        }
        return currentUser.id();
    }

    public long requireEmployeeId() {
        final var currentUser = this.requireCurrentUser();
        if (currentUser.role() != PersonRole.EMPLOYEE) {
            throw new SecurityException("Employee role required");
        }
        return currentUser.id();
    }

    public long requireAdminId() {
        final var currentUser = this.requireCurrentUser();
        if (currentUser.role() != PersonRole.ADMIN) {
            throw new SecurityException("Admin role required");
        }
        return currentUser.id();
    }
}
