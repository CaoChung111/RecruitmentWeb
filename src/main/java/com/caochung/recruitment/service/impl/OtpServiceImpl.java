package com.caochung.recruitment.service.impl;

import com.caochung.recruitment.service.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "OPT-SERVICE")
public class OtpServiceImpl implements OtpService {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String FORGOT_PASSWORD_PREFIX = "forgot_password";
    private static final String REGISTER_DATA_PREFIX = "register_data";
    private static final String REGISTER_OTP_PREFIX = "register_otp";
    private static final String REGISTER_COOLDOWN_PREFIX = "register_cooldown";
    @Override
    public void savePasswordOtp(String email, String otp) {
        String key = FORGOT_PASSWORD_PREFIX + email;
        redisTemplate.opsForValue().set(key, otp, Duration.ofMinutes(5));
        log.info("Saved OTP into redis for email: {}", email);
    }

    @Override
    public String getPasswordOtp(String email) {
        String key = FORGOT_PASSWORD_PREFIX + email;
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void deletePasswordOtp(String email) {
        String key = FORGOT_PASSWORD_PREFIX + email;
        redisTemplate.delete(key);
    }

    @Override
    public void saveRegisterData(String email, String otp, String userJson){
        redisTemplate.opsForValue().set(REGISTER_DATA_PREFIX + email, userJson, Duration.ofMinutes(5));
        redisTemplate.opsForValue().set(REGISTER_OTP_PREFIX + email, otp, Duration.ofMinutes(5));
        redisTemplate.opsForValue().set(REGISTER_COOLDOWN_PREFIX + email, "wait", Duration.ofMinutes(60));
    }

    @Override
    public boolean isSpamming(String email){
        return Boolean.TRUE.equals(redisTemplate.opsForValue().get(REGISTER_COOLDOWN_PREFIX + email));
    }

    @Override
    public String getRegisterOtp(String email){
        return redisTemplate.opsForValue().get(REGISTER_OTP_PREFIX + email);
    }

    @Override
    public String getRegisterData(String email){
        return redisTemplate.opsForValue().get(REGISTER_DATA_PREFIX + email);
    }

    @Override
    public void clearRegistrationData(String email) {
        redisTemplate.delete(REGISTER_OTP_PREFIX + email);
        redisTemplate.delete(REGISTER_DATA_PREFIX + email);
    }
}
