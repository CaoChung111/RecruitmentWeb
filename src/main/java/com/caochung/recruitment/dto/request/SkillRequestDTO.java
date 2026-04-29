package com.caochung.recruitment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SkillRequestDTO {
    @NotBlank(message = "Name cannot be empty")
    @Size(min = 1, max = 100, message = "Skill name must be between 1 and 100 characters")
    private String name;
}
