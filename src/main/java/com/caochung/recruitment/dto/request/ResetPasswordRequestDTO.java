package com.caochung.recruitment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Builder
public class ResetPasswordRequestDTO {
    @NotBlank(message = "Email cannot be empty")
    private String email;
    @NotBlank(message = "OTP cannot be empty")
    private String otp;
    @NotBlank(message = "New password cannot be empty")
    private String newPassword;
}
