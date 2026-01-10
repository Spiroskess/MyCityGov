package gr.hua.dit.mycitygov.core.security;


import gr.hua.dit.mycitygov.core.model.PersonRole;

/**
 * @see CurrentUserProvider
 */
public record CurrentUser(long id, String emailAddress, PersonRole role) {}
