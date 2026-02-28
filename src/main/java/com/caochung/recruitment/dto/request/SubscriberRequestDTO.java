package com.caochung.recruitment.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SubscriberRequestDTO {
    @NotBlank(message = "Name cannot be empty")
    private String name;

    @Email(message = "Email is not in the correct format.")
    @NotBlank(message = "Email cannot be empty")
    private String email;

    @NotEmpty(message = "Skill cannot be empty")
    private List<Long> skills;
}