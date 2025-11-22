package gr.hua.dit.mycitygov.core.service.impl;

import gr.hua.dit.mycitygov.core.model.Citizen;
import gr.hua.dit.mycitygov.core.model.Role;
import gr.hua.dit.mycitygov.core.repository.CitizenRepository;
import gr.hua.dit.mycitygov.core.service.CitizenService;
import gr.hua.dit.mycitygov.core.service.model.RegistrationRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CitizenServiceImpl implements CitizenService {

    private final CitizenRepository citizenRepository;
    private final PasswordEncoder passwordEncoder;

    public CitizenServiceImpl(CitizenRepository citizenRepository,
                              PasswordEncoder passwordEncoder) {
        this.citizenRepository = citizenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Citizen registerCitizen(RegistrationRequest request) {
        citizenRepository.findByEmail(request.getEmail()).ifPresent(c -> {
            throw new IllegalArgumentException("Email already in use");
        });

        Citizen c = new Citizen();
        c.setEmail(request.getEmail());
        c.setPassword(passwordEncoder.encode(request.getPassword()));
        c.setFullName(request.getFullName());
        c.setPhone(request.getPhone());
        c.setRole(Role.CITIZEN);

        return citizenRepository.save(c);
    }

    @Override
    public Citizen getOrCreateByEmail(String email, String fullNameIfNew) {
        return citizenRepository.findByEmail(email)
            .orElseGet(() -> {
                Citizen c = new Citizen();
                c.setEmail(email);
                c.setPassword(passwordEncoder.encode("changeme"));
                c.setFullName(fullNameIfNew != null ? fullNameIfNew : email);
                c.setRole(Role.CITIZEN);
                return citizenRepository.save(c);
            });
    }
}
