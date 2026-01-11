package gr.hua.dit.mycitygov.core.repository;

import gr.hua.dit.mycitygov.core.model.RequestAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RequestAttachmentRepository extends JpaRepository<RequestAttachment, Long> {

    // Συνημμένα ενός αιτήματος
    List<RequestAttachment> findByRequestIdOrderByUploadedAtAsc(Long requestId);

    // Βρες συνημμένο μόνο αν ανήκει στο συγκεκριμένο αίτημα
    Optional<RequestAttachment> findByIdAndRequestId(Long id, Long requestId);
}
