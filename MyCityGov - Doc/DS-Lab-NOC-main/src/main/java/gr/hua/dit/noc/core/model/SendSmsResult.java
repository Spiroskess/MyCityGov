package gr.hua.dit.noc.core.model;

/**
 * SendSmsResult DTO.
 *
 * @author Dimitris Gkoulis
 * @see gr.hua.dit.noc.core.SmsService
 * @see gr.hua.dit.noc.core.impl.RouteeSmsService
 * @see gr.hua.dit.noc.core.impl.MockSmsService
 */
public record SendSmsResult(boolean sent) {}
