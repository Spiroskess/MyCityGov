package gr.hua.dit.mycitygov.core.service.impl;

import gr.hua.dit.mycitygov.core.model.Person;
import gr.hua.dit.mycitygov.core.model.Request;
import gr.hua.dit.mycitygov.core.model.RequestAttachment;
import gr.hua.dit.mycitygov.core.repository.RequestAttachmentRepository;
import gr.hua.dit.mycitygov.core.repository.RequestRepository;
import gr.hua.dit.mycitygov.core.service.FileStorageService;
import gr.hua.dit.mycitygov.core.service.RequestAttachmentService;
import gr.hua.dit.mycitygov.core.service.mapper.RequestAttachmentMapper;
import gr.hua.dit.mycitygov.core.service.model.AttachmentDownload;
import gr.hua.dit.mycitygov.core.service.model.AttachmentUpload;
import gr.hua.dit.mycitygov.core.service.model.AttachmentView;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class RequestAttachmentServiceImpl implements RequestAttachmentService {

    private final RequestRepository requestRepository;
    private final RequestAttachmentRepository attachmentRepository;
    private final FileStorageService fileStorageService;
    private final RequestAttachmentMapper mapper;

    public RequestAttachmentServiceImpl(RequestRepository requestRepository,
                                        RequestAttachmentRepository attachmentRepository,
                                        FileStorageService fileStorageService,
                                        RequestAttachmentMapper mapper) {
        this.requestRepository = requestRepository;
        this.attachmentRepository = attachmentRepository;
        this.fileStorageService = fileStorageService;
        this.mapper = mapper;
    }

    @Override
    public void addForCitizenRequest(Long requestId, Person citizen, AttachmentUpload upload) {
        Request req = requestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("REQUEST_NOT_FOUND"));

        if (req.getCitizen() == null || !req.getCitizen().getId().equals(citizen.getId())) {
            throw new AccessDeniedException("NOT_YOUR_REQUEST");
        }

        String original = (upload.originalFilename() == null || upload.originalFilename().isBlank())
            ? "file"
            : upload.originalFilename();

        original = original.replace("\\", "_").replace("/", "_");

        String key = "requests/" + requestId + "/" + UUID.randomUUID() + "_" + original;

        // 1) upload στο MinIO
        fileStorageService.put(key, upload.inputStream(), upload.sizeBytes(), upload.contentType());

        // 2) αποθήκευση metadata στη DB
        RequestAttachment a = new RequestAttachment();
        a.setRequest(req);
        a.setObjectKey(key);
        a.setOriginalFilename(original);
        a.setContentType(upload.contentType());
        a.setSizeBytes(upload.sizeBytes());

        attachmentRepository.save(a);
    }

    @Override
    public List<AttachmentView> listForCitizenRequest(Long requestId, Person citizen) {
        Request req = requestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("REQUEST_NOT_FOUND"));

        if (req.getCitizen() == null || !req.getCitizen().getId().equals(citizen.getId())) {
            throw new AccessDeniedException("NOT_YOUR_REQUEST");
        }

        return attachmentRepository.findByRequestIdOrderByUploadedAtAsc(requestId)
            .stream().map(mapper::toView).toList();
    }

    @Override
    public List<AttachmentView> listForEmployeeRequest(Long requestId, Person employee) {
        Request req = requestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("REQUEST_NOT_FOUND"));

        // επιτρέπουμε download μόνο αν είναι assigned στον υπάλληλο
        if (req.getAssignedEmployee() == null || !req.getAssignedEmployee().getId().equals(employee.getId())) {
            throw new AccessDeniedException("NOT_ASSIGNED_TO_YOU");
        }

        return attachmentRepository.findByRequestIdOrderByUploadedAtAsc(requestId)
            .stream().map(mapper::toView).toList();
    }

    @Override
    public AttachmentDownload downloadForCitizen(Long requestId, Long attachmentId, Person citizen) {
        Request req = requestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("REQUEST_NOT_FOUND"));

        if (req.getCitizen() == null || !req.getCitizen().getId().equals(citizen.getId())) {
            throw new AccessDeniedException("NOT_YOUR_REQUEST");
        }

        RequestAttachment a = attachmentRepository.findByIdAndRequestId(attachmentId, requestId)
            .orElseThrow(() -> new IllegalArgumentException("ATTACHMENT_NOT_FOUND"));

        var in = fileStorageService.get(a.getObjectKey());
        String ct = (a.getContentType() == null || a.getContentType().isBlank())
            ? "application/octet-stream"
            : a.getContentType();

        return new AttachmentDownload(a.getOriginalFilename(), ct, a.getSizeBytes(), in);
    }

    @Override
    public AttachmentDownload downloadForEmployee(Long requestId, Long attachmentId, Person employee) {
        Request req = requestRepository.findById(requestId)
            .orElseThrow(() -> new IllegalArgumentException("REQUEST_NOT_FOUND"));

        if (req.getAssignedEmployee() == null || !req.getAssignedEmployee().getId().equals(employee.getId())) {
            throw new AccessDeniedException("NOT_ASSIGNED_TO_YOU");
        }

        RequestAttachment a = attachmentRepository.findByIdAndRequestId(attachmentId, requestId)
            .orElseThrow(() -> new IllegalArgumentException("ATTACHMENT_NOT_FOUND"));

        var in = fileStorageService.get(a.getObjectKey());
        String ct = (a.getContentType() == null || a.getContentType().isBlank())
            ? "application/octet-stream"
            : a.getContentType();

        return new AttachmentDownload(a.getOriginalFilename(), ct, a.getSizeBytes(), in);
    }
}
