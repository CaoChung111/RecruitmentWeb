package com.caochung.recruitment.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SkillRequestDTO {
    @NotBlank(message = "Name cannot be empty")
    private String name;
}
