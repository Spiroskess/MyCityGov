package gr.hua.dit.mycitygov.core.service.mapper;

import gr.hua.dit.mycitygov.core.model.RequestAttachment;
import gr.hua.dit.mycitygov.core.service.model.AttachmentView;
import org.springframework.stereotype.Component;

@Component
public class RequestAttachmentMapper {
    // Mapper: μετατρέπει RequestAttachment entity σε AttachmentView DTO
    public AttachmentView toView(RequestAttachment a) {
        return new AttachmentView(
            a.getId(),
            a.getOriginalFilename(),
            a.getContentType(),
            a.getSizeBytes(),
            a.getUploadedAt()
        );
    }
}
