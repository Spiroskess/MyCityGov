package gr.hua.dit.noc.core.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for requesting SMS send.
 *
 * @author Dimitris Gkoulis
 * @see gr.hua.dit.noc.core.SmsService
 * @see gr.hua.dit.noc.core.impl.RouteeSmsService
 * @see gr.hua.dit.noc.core.impl.MockSmsService
 */
public record SendSmsRequest(
    @NotNull @NotBlank String e164,
    @NotNull @NotBlank String content
) {}
