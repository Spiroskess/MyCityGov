package gr.hua.dit.mycitygov.core.security;

import gr.hua.dit.mycitygov.core.model.PersonType;

/**
 * @see CurrentUserProvider
 */
public record CurrentUser(long id, String emailAddress, PersonType type) {}
