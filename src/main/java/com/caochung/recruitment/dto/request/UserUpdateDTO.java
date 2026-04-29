package com.caochung.recruitment.dto.request;

import com.caochung.recruitment.domain.Company;
import com.caochung.recruitment.util.annotation.Gender;
import com.caochung.recruitment.util.annotation.UserStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import static com.caochung.recruitment.constant.GenderEnum.*;
import static com.caochung.recruitment.constant.UserStatusEnum.*;

@Getter @Setter
public class UserUpdateDTO {
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Min(value = 18, message = "Age must be at least 18")
    @Max(value = 100, message = "Age must be at most 100")
    private int age;

    @Gender(anyOf = {MALE, FEMALE, OTHER})
    private String gender;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    private Long companyId;

    private Long role;
}
