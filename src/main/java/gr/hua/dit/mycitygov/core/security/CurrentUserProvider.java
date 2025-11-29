package gr.hua.dit.mycitygov.core.security;


import gr.hua.dit.mycitygov.core.model.Person;
import gr.hua.dit.mycitygov.core.model.PersonRole;
import gr.hua.dit.mycitygov.core.repository.PersonRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Component for providing the current user.
 *
 * @see CurrentUser
 */
@Component
public final class CurrentUserProvider {

    private final PersonRepository personRepository;

    public CurrentUserProvider(final PersonRepository personRepository) {
        if (personRepository == null) throw new NullPointerException("personRepository");
        this.personRepository = personRepository;
    }

    /**
     * Returns a lightweight view of the currently authenticated user, if any.
     */
    public Optional<CurrentUser> getCurrentUser() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return Optional.empty();
        }
        if (authentication.getPrincipal() instanceof ApplicationUserDetails userDetails) {
            return Optional.of(new CurrentUser(
                userDetails.personId(),
                userDetails.getUsername(),
                userDetails.role()
            ));
        }
        return Optional.empty();
    }

    /**
     * Returns the current user or throws {@link SecurityException} if not authenticated.
     */
    public CurrentUser requireCurrentUser() {
        return this.getCurrentUser()
            .orElseThrow(() -> new SecurityException("not authenticated"));
    }

    /**
     * Returns the Person entity of the current authenticated user, if present.
     */
    public Optional<Person> getCurrentPerson() {
        return getCurrentUser()
            .flatMap(u -> personRepository.findById(u.id()));
    }

    /**
     * Convenience helpers for role-specific access (χρήσιμα αν τα χρειαστείς).
     */
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
