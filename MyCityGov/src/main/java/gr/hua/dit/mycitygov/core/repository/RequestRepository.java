package gr.hua.dit.mycitygov.core.repository;

import gr.hua.dit.mycitygov.core.model.Person;
import gr.hua.dit.mycitygov.core.model.Request;
import gr.hua.dit.mycitygov.core.model.RequestStatus;
import gr.hua.dit.mycitygov.core.service.model.MunicipalService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    // Αιτήματα ανά πολίτη/υπάλληλο για “βλέπω τα δικά μου”
    List<Request> findAllByCitizenOrderByCreatedAtDesc(Person citizen);
    List<Request> findAllByAssignedEmployeeOrderByCreatedAtDesc(Person employee);

    // Φίλτρα για dashboards/reports κατάσταση/υπηρεσία
    List<Request> findAllByStatusOrderByCreatedAtDesc(RequestStatus status);
    List<Request> findAllByAssignedServiceOrderByCreatedAtDesc(MunicipalService service);

    // Αιτήματα υπηρεσίας που δεν έχουν ανατεθεί σε υπάλληλο ακόμα
    List<Request> findAllByAssignedServiceAndAssignedEmployeeIsNullOrderByCreatedAtDesc(MunicipalService service);

    // Ownership checks (ασφάλεια: να μην ανοίγει κάποιος αίτημα που δεν του ανήκει)
    Optional<Request> findByIdAndAssignedEmployee(Long id, Person employee);
    Optional<Request> findByIdAndCitizen(Long id, Person citizen);

    // Γενικές λίστες για admin/στατιστικά
    List<Request> findAllByOrderByCreatedAtDesc();
    List<Request> findAllByAssignedEmployeeIsNullOrderByCreatedAtDesc();
    List<Request> findAllByAssignedEmployeeIsNotNullOrderByCreatedAtDesc();
    List<Request> findAllByAssignedServiceIsNullOrderByCreatedAtDesc();
    List<Request> findAllByAssignedServiceIsNotNullOrderByCreatedAtDesc();
}
