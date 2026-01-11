package gr.hua.dit.mycitygov.core.service;

import gr.hua.dit.mycitygov.core.model.Person;
import gr.hua.dit.mycitygov.core.service.model.AttachmentDownload;
import gr.hua.dit.mycitygov.core.service.model.AttachmentUpload;
import gr.hua.dit.mycitygov.core.service.model.AttachmentView;

import java.util.List;

// Service interface για συνημμένα αιτημάτων
public interface RequestAttachmentService {

    void addForCitizenRequest(Long requestId, Person citizen, AttachmentUpload upload);

    void addAdditionalInfoForCitizenRequest(Long requestId, Person citizen, AttachmentUpload upload);

    List<AttachmentView> listForCitizenRequest(Long requestId, Person citizen);

    List<AttachmentView> listForEmployeeRequest(Long requestId, Person employee);

    AttachmentDownload downloadForCitizen(Long requestId, Long attachmentId, Person citizen);

    AttachmentDownload downloadForEmployee(Long requestId, Long attachmentId, Person employee);
}
