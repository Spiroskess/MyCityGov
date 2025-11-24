package gr.hua.dit.mycitygov.core.repository;

import gr.hua.dit.mycitygov.core.model.Citizen;
import gr.hua.dit.mycitygov.core.model.Request;
import gr.hua.dit.mycitygov.core.model.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findByCitizenOrderBySubmittedAtDesc(Citizen citizen);

    List<Request> findByStatus(RequestStatus status);

    // Αιτήματα που έχει αναλάβει ένας υπάλληλος
    List<Request> findByAssignedEmployeeOrderBySubmittedAtDesc(Citizen employee);

    // Όλα τα αιτήματα, ταξινομημένα με βάση την ημερομηνία
    List<Request> findAllByOrderBySubmittedAtDesc();
}
