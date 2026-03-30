package com.caochung.recruitment.service;

import org.springframework.data.redis.core.ZSetOperations;

import java.util.Set;

public interface RedisService {
    void savePasswordOtp(String email, String otp);
    String getPasswordOtp(String email);
    void deletePasswordOtp(String email);

    void saveRegisterData(String email, String otp, String userJson);

    boolean isSpamming(String email);

    String getRegisterOtp(String email);

    String getRegisterData(String email);

    void clearRegistrationData(String email);

    void createBacklistToken(String token, long expiration);

    boolean isExpireToken(String token);

    void countViewJob(Long jobId);

    Set<String> getJobTrendings();

}
