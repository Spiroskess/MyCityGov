package gr.hua.dit.mycitygov.core.service;

import gr.hua.dit.mycitygov.core.model.Citizen;
import gr.hua.dit.mycitygov.core.service.model.RegistrationRequest;

public interface CitizenService {

    Citizen registerCitizen(RegistrationRequest request);

    Citizen getOrCreateByEmail(String email, String fullNameIfNew);
}
