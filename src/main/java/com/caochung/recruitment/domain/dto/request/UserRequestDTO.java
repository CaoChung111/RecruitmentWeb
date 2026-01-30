package com.caochung.recruitment.domain.dto.request;

import com.caochung.recruitment.constant.GenderEnum;
import com.caochung.recruitment.util.annotation.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

import static com.caochung.recruitment.constant.GenderEnum.FEMALE;
import static com.caochung.recruitment.constant.GenderEnum.MALE;
import static com.caochung.recruitment.constant.GenderEnum.OTHER;

@Getter
@Setter
public class UserRequestDTO implements Serializable {
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
