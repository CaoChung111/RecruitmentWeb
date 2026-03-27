package com.caochung.recruitment.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class ForgotPasswordEvent {
    private String email;
    private String otpToken;
}
