package gr.hua.dit.mycitygov.core.service;

import gr.hua.dit.mycitygov.core.model.Person;

public interface GovUiLoginService {

    // Service interface για gov-login στο UI
    void pingProvider(); // optional GET προς external

    Person loginOrRegisterCitizenByToken(String userToken);

    default Person loginOrRegisterCitizen(String afm, String amka, String lastName) {
        throw new UnsupportedOperationException("Use loginOrRegisterCitizenByToken(userToken)");
    }
}
