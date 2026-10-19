package com.sales.request.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum NOTIFICATION_EVENT_FOR {
    LOGIN_OTP("LOGIN_OTP_EMAIL","LOGIN_OTP_PUSH","LOGIN_OTP_SMS"),
    FILE_EXPORT_COMPLETE("FILE_EXPORT_COMPLETE_EMAIL","FILE_EXPORT_COMPLETE_PUSH","FILE_EXPORT_COMPLETE_SMS");
    public String email;
    public String push;
    public String sms;
}
