package com.caochung.recruitment.dto.request;

import com.caochung.recruitment.util.annotation.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import static com.caochung.recruitment.constant.GenderEnum.*;

@Getter
@Setter
@Builder
public class RegisterDTO {
    @NotBlank(message = "Username cannot be empty")
    private  String name;

    @Email(message = "Email cannot be empty")
    private String email;

    @NotBlank(message = "Password cannot be empty")
    private String password;

    private int age;

    @Gender(anyOf = {MALE, FEMALE, OTHER})
    private String gender;

    private String address;
}
