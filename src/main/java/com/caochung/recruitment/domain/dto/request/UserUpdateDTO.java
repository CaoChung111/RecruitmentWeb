package com.caochung.recruitment.domain.dto.request;

import com.caochung.recruitment.util.annotation.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import static com.caochung.recruitment.constant.GenderEnum.*;

public class UserUpdateDTO {
    private  String name;

    private int age;

    @Gender(anyOf = {MALE, FEMALE, OTHER})
    private String gender;

    private String address;
}
