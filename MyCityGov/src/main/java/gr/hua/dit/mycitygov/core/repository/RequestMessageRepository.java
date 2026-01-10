package gr.hua.dit.mycitygov.core.repository;

import gr.hua.dit.mycitygov.core.model.Request;
import gr.hua.dit.mycitygov.core.model.RequestMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestMessageRepository extends JpaRepository<RequestMessage, Long> {

    List<RequestMessage> findAllByRequestAndVisibleToCitizenOrderByCreatedAtAsc(Request request, boolean visibleToCitizen);

    List<RequestMessage> findAllByRequestOrderByCreatedAtAsc(Request request);
}
