package com.caochung.recruitment.dto.request;

import com.caochung.recruitment.util.annotation.Gender;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import static com.caochung.recruitment.constant.GenderEnum.*;

@Getter
@Setter
@Builder
public class RegisterDTO {
    @NotBlank(message = "Name cannot be empty")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Email is not in the correct format")
    private String email;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    @Min(value = 18, message = "Age must be at least 18")
    @Max(value = 100, message = "Age must be at most 100")
    private int age;

    @Gender(anyOf = {MALE, FEMALE, OTHER})
    private String gender;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;
}
