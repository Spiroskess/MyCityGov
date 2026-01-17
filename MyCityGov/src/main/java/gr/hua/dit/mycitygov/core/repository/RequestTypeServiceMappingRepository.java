package gr.hua.dit.mycitygov.core.repository;

import gr.hua.dit.mycitygov.core.model.RequestTypeServiceMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RequestTypeServiceMappingRepository extends JpaRepository<RequestTypeServiceMapping, Long> {

    Optional<RequestTypeServiceMapping> findByRequestType_Code(String requestTypeCode);

    void deleteByRequestType_Code(String requestTypeCode);
}
