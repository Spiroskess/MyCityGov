package gr.hua.dit.mycitygov.core.repository;

import gr.hua.dit.mycitygov.core.model.RequestTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RequestTypeRepository extends JpaRepository<RequestTypeEntity, Long> {

    Optional<RequestTypeEntity> findByCode(String code);

    boolean existsByCode(String code);

    List<RequestTypeEntity> findAllByEnabledTrueOrderByIdAsc();

    List<RequestTypeEntity> findAllByOrderByIdAsc();
}
