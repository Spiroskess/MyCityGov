package gr.hua.dit.mockgov.repository;

import gr.hua.dit.mockgov.api.CitizenIdentityDto;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Primary
@Repository
public class DbCitizenDirectory implements CitizenDirectory {

    private final GovCitizenRepository repo;

    public DbCitizenDirectory(GovCitizenRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<CitizenIdentityDto> findByCredentials(String afm, String amka, String lastName) {
        String a = digitsOnly(afm);
        String m = digitsOnly(amka);
        String ln = (lastName == null) ? "" : lastName.trim();

        // 9 ψηφία ΑΦΜ, 11 ψηφία ΑΜΚΑ
        if (!a.matches("\\d{9}") || !m.matches("\\d{11}") || ln.isBlank()) {
            return Optional.empty();
        }

        return repo.findByAfmAndAmka(a, m)
                .filter(e -> e.getLastName() != null && e.getLastName().equalsIgnoreCase(ln))
                .map(e -> new CitizenIdentityDto(e.getAfm(), e.getAmka(), e.getFirstName(), e.getLastName()));
    }

    private String digitsOnly(String s) {
        if (s == null) return "";
        return s.replaceAll("\\D", "");
    }
}
