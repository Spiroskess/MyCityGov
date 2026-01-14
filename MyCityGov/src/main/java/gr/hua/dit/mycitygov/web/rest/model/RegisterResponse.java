package gr.hua.dit.mycitygov.web.rest.model;

import gr.hua.dit.mycitygov.core.service.model.PersonView;

public class RegisterResponse {

    public String token;
    public PersonView person;

    public RegisterResponse(String token, PersonView person) {
        this.token = token;
        this.person = person;
    }
}
