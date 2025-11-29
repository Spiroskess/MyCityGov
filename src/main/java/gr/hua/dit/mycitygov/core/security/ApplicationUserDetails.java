package gr.hua.dit.mycitygov.core.security;

import gr.hua.dit.mycitygov.core.model.PersonRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Immutable view implementing Spring's {@link UserDetails} for representing a user in runtime.
 */
@SuppressWarnings("RedundantMethodOverride")
public final class ApplicationUserDetails implements UserDetails {

    private final long personId;
    private final String emailAddress;
    private final String passwordHash;
    private final PersonRole role;

    public ApplicationUserDetails(final long personId,
                                  final String emailAddress,
                                  final String passwordHash,
                                  final PersonRole role) {
        if (personId <= 0) throw new IllegalArgumentException("personId must be > 0");
        if (emailAddress == null) throw new NullPointerException("emailAddress");
        if (emailAddress.isBlank()) throw new IllegalArgumentException("emailAddress is blank");
        if (passwordHash == null) throw new NullPointerException("passwordHash");
        if (passwordHash.isBlank()) throw new IllegalArgumentException("passwordHash is blank");
        if (role == null) throw new NullPointerException("role");

        this.personId = personId;
        this.emailAddress = emailAddress;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public long personId() {
        return this.personId;
    }

    public PersonRole role() {
        return this.role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        final String roleName;
        if (this.role == PersonRole.ADMIN) roleName = "ROLE_ADMIN";
        else if (this.role == PersonRole.EMPLOYEE) roleName = "ROLE_EMPLOYEE";
        else if (this.role == PersonRole.CITIZEN) roleName = "ROLE_CITIZEN";
        else throw new RuntimeException("Invalid role: " + this.role);
        return Collections.singletonList(new SimpleGrantedAuthority(roleName));
    }

    @Override
    public String getPassword() {
        return this.passwordHash;
    }

    @Override
    public String getUsername() {
        return this.emailAddress;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
