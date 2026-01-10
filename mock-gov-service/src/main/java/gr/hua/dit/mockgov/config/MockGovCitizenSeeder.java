package gr.hua.dit.mockgov.config;

import gr.hua.dit.mockgov.repository.GovCitizenEntity;
import gr.hua.dit.mockgov.repository.GovCitizenRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MockGovCitizenSeeder implements CommandLineRunner {

    private final GovCitizenRepository repo;

    public MockGovCitizenSeeder(GovCitizenRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) {
        if (repo.count() > 0) return;

        // Βάλε εδώ τους πολίτες που θες να "υπάρχουν" στο MockGov DB
        repo.save(new GovCitizenEntity("123456789", "13050500571", "Spyros", "Kesidis"));
        repo.save(new GovCitizenEntity("789456123", "78945612378", "Rimma", "Tsotsioulidou"));
    }
}
