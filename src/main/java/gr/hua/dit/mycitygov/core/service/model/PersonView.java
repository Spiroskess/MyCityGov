package gr.hua.dit.mycitygov.core.service.model;

import gr.hua.dit.mycitygov.core.model.PersonRole;

/**
 * General view of {@link gr.hua.dit.mycitygov.core.model.Person} entity.
 *
 * @see gr.hua.dit.mycitygov.core.model.Person
 * @see gr.hua.dit.mycitygov.core.service.PersonService
 */
public record PersonView(
    long id,
    String firstName,
    String lastName,
    String mobilePhoneNumber,
    String emailAddress,
    PersonRole role
) {

    public String fullName() {
        return this.firstName + " " + this.lastName;
    }
}
