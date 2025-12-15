package gr.hua.dit.mycitygov.core.port;

public interface SmsNotificationPort {
    boolean sendSms(String e164, String content);
}
