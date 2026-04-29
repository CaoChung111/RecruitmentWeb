package com.caochung.recruitment.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyOtpDTO {
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Email is not in the correct format")
    private String email;

    @NotBlank(message = "Otp cannot be empty")
    @Pattern(regexp = "\\d{6}", message = "OTP must be exactly 6 digits")
    private String otp;
}
