package gr.hua.dit.mycitygov.core.service.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateRequestRequest {

    @NotNull
    private Long requestTypeId;

    @NotBlank
    @Size(max = 255)
    private String title;

    @Size(max = 4000)
    private String description;

    public Long getRequestTypeId() { return requestTypeId; }

    public void setRequestTypeId(Long requestTypeId) { this.requestTypeId = requestTypeId; }

    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }
}
