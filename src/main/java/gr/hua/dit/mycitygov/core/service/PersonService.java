package gr.hua.dit.mycitygov.core.service;

import gr.hua.dit.mycitygov.core.service.model.CreatePersonRequest;
import gr.hua.dit.mycitygov.core.service.model.CreatePersonResult;

public interface PersonService {

    CreatePersonResult createPerson(CreatePersonRequest createPersonRequest, boolean notify);

    default CreatePersonResult createPerson(CreatePersonRequest createPersonRequest) {
        return createPerson(createPersonRequest, true);
    }
}
