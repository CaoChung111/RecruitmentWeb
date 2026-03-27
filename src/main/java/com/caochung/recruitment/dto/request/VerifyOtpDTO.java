package com.caochung.recruitment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyOtpDTO {
    @NotBlank(message = "Email cannot be empty")
    private String email;

    @NotBlank(message = "Otp cannot be empty")
    private String otp;
}
