package com.caochung.recruitment.service;

import com.caochung.recruitment.dto.request.RegisterDTO;
import com.caochung.recruitment.dto.request.ResetPasswordRequestDTO;
import com.caochung.recruitment.dto.request.VerifyOtpDTO;
import com.caochung.recruitment.dto.response.UserResponseDTO;
import org.springframework.transaction.annotation.Transactional;

public interface AuthService {
    @Transactional(rollbackFor = Exception.class)
    UserResponseDTO registerAndSendOtp(RegisterDTO registerDTO);

    @Transactional
    void VerifyOtp(VerifyOtpDTO verifyOtpDTO);

    void forgotPassword(String email);
    void resetPassword(ResetPasswordRequestDTO resetPasswordRequestDTO);
}
