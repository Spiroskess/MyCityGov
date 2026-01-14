package gr.hua.dit.mycitygov.web.rest.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public class CitizenAdditionalInfoRestRequest {

    @Min(0)
    @Schema(example = "1")
    public int uploadedFilesCount;

    @Size(max = 2000)
    @Schema(example = "Σας επισυνάπτω το έγγραφο...", nullable = true)
    public String note;
}
