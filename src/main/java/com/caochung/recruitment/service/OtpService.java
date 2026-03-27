package com.caochung.recruitment.service;

public interface OtpService {
    void savePasswordOtp(String email, String otp);
    String getPasswordOtp(String email);
    void deletePasswordOtp(String email);

    void saveRegisterData(String email, String otp, String userJson);

    boolean isSpamming(String email);

    String getRegisterOtp(String email);

    String getRegisterData(String email);

    void clearRegistrationData(String email);
}
