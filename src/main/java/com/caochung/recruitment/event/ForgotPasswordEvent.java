package com.caochung.recruitment.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@Builder
public class ForgotPasswordEvent {
    private String email;
    private String otpToken;
}
