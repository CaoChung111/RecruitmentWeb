package com.caochung.recruitment.dto.request;

import com.caochung.recruitment.domain.Company;
import com.caochung.recruitment.util.annotation.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import static com.caochung.recruitment.constant.GenderEnum.*;

@Getter @Setter
public class UserUpdateDTO {
    private  String name;

    private int age;

    @Gender(anyOf = {MALE, FEMALE, OTHER})
    private String gender;

    private String address;

    private Long companyId;
}
