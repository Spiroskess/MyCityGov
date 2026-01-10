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

    List<Request> findAllByCitizenOrderByCreatedAtDesc(Person citizen);

    List<Request> findAllByAssignedEmployeeOrderByCreatedAtDesc(Person employee);

    List<Request> findAllByStatusOrderByCreatedAtDesc(RequestStatus status);

    List<Request> findAllByAssignedServiceOrderByCreatedAtDesc(MunicipalService service);

    List<Request> findAllByAssignedServiceAndAssignedEmployeeIsNullOrderByCreatedAtDesc(MunicipalService service);

    Optional<Request> findByIdAndAssignedEmployee(Long id, Person employee);
    Optional<Request> findByIdAndCitizen(Long id, Person citizen);

    List<Request> findAllByOrderByCreatedAtDesc();

    List<Request> findAllByAssignedEmployeeIsNullOrderByCreatedAtDesc();

    List<Request> findAllByAssignedEmployeeIsNotNullOrderByCreatedAtDesc();
}
