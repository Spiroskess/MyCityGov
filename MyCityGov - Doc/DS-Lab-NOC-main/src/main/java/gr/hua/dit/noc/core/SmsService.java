package gr.hua.dit.noc.core;

import gr.hua.dit.noc.core.model.SendSmsRequest;
import gr.hua.dit.noc.core.model.SendSmsResult;

/**
 * Service for sending SMS.
 *
 * @author Dimitris Gkoulis
 */
public interface SmsService {

    SendSmsResult send(final SendSmsRequest sendSmsRequest);
}
