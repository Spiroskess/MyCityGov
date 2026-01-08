package gr.hua.dit.mockgov.repository;

import gr.hua.dit.mockgov.api.CitizenIdentityDto;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryCitizenDirectory implements CitizenDirectory {

    private final Map<String, CitizenIdentityDto> citizensByKey = new ConcurrentHashMap<>();

    public InMemoryCitizenDirectory() {
        // Demo πολίτες (μένουν όπως είναι)
        put(new CitizenIdentityDto("123456789", "12345678912", "Giannis", "Kesidis"));
        put(new CitizenIdentityDto("987654321", "02028854321", "Γιώργος", "Ιωάννου"));
        put(new CitizenIdentityDto("111222333", "15079567890", "Ελένη", "Κωνσταντίνου"));
    }

    @Override
    public Optional<CitizenIdentityDto> findByCredentials(String afm, String amka, String lastName) {
        String a = digitsOnly(afm);
        String m = digitsOnly(amka);
        String ln = (lastName == null) ? "" : lastName.trim();

        // Basic sanity (9 ψηφία ΑΦΜ, 11 ψηφία ΑΜΚΑ)
        if (!a.matches("\\d{9}") || !m.matches("\\d{11}") || ln.isBlank()) {
            return Optional.empty();
        }

        String key = key(a, m);

        // Αν υπάρχει ήδη, απαιτούμε να ταιριάζει το επώνυμο
        CitizenIdentityDto existing = citizensByKey.get(key);
        if (existing != null) {
            if (existing.lastName() != null && existing.lastName().equalsIgnoreCase(ln)) {
                return Optional.of(existing);
            }
            return Optional.empty();
        }

        //  Demo-friendly: αν δεν υπάρχει, τον “δημιουργούμε” (ώστε να δουλεύει με στοιχεία από MyCityGov DB)
        CitizenIdentityDto created = new CitizenIdentityDto(a, m, "Demo", ln);
        citizensByKey.putIfAbsent(key, created);
        return Optional.of(created);
    }

    private void put(CitizenIdentityDto c) {
        citizensByKey.put(key(c.afm(), c.amka()), c);
    }

    private String key(String afm, String amka) {
        return afm + "|" + amka;
    }

    private String digitsOnly(String s) {
        if (s == null) return "";
        return s.replaceAll("\\D", "");
    }
}
