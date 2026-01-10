package gr.hua.dit.mockgov.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GovCitizenRepository extends JpaRepository<GovCitizenEntity, Long> {
    Optional<GovCitizenEntity> findByAfmAndAmka(String afm, String amka);
}
