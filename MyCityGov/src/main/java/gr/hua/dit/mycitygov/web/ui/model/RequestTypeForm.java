package gr.hua.dit.mycitygov.web.ui.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RequestTypeForm {

    @NotBlank
    @Size(max = 64)
    private String code;

    @NotBlank
    @Size(max = 160)
    private String title;

    @NotNull
    @Min(1)
    @Max(3650)
    private Integer slaDays;

    private boolean enabled = true;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getSlaDays() { return slaDays; }
    public void setSlaDays(Integer slaDays) { this.slaDays = slaDays; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
