package gr.hua.dit.mycitygov.core.port.impl.dto;

public record SendSmsResult(boolean sent, String provider, String messageId) {}
