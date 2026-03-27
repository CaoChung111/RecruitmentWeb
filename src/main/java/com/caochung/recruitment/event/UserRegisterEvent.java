package com.caochung.recruitment.event;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserRegisterEvent {
    private String email;
    private String otpToken;
}
