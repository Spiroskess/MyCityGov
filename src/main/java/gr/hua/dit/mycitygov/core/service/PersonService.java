package gr.hua.dit.mycitygov.core.service;

import gr.hua.dit.mycitygov.core.service.model.CreatePersonRequest;
import gr.hua.dit.mycitygov.core.service.model.CreatePersonResult;

/**
 * Service for managing {@link gr.hua.dit.mycitygov.core.model.Person}.
 */
public interface PersonService {

    CreatePersonResult createPerson(final CreatePersonRequest createPersonRequest, final boolean notify);

    default CreatePersonResult createPerson(final CreatePersonRequest createPersonRequest) {
        return this.createPerson(createPersonRequest, true);
    }
}
