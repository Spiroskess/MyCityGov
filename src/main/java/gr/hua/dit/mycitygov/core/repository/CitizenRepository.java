package gr.hua.dit.mycitygov.core.repository;

import gr.hua.dit.mycitygov.core.model.Citizen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CitizenRepository extends JpaRepository<Citizen, Long> {

    Optional<Citizen> findByEmail(String email);
}
