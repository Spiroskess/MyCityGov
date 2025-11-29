package gr.hua.dit.mycitygov.core.repository;

import gr.hua.dit.mycitygov.core.model.Person;
import gr.hua.dit.mycitygov.core.model.Request;
import gr.hua.dit.mycitygov.core.model.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findAllByCitizenOrderByCreatedAtDesc(Person citizen);

    List<Request> findAllByAssignedEmployeeOrderByCreatedAtDesc(Person employee);

    List<Request> findAllByStatusOrderByCreatedAtDesc(RequestStatus status);
}
