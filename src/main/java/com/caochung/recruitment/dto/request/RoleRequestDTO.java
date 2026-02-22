package com.caochung.recruitment.dto.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class RoleRequestDTO {
    @NotBlank(message = "Name cannot be empty")
    private String name;

    private String description;

    private boolean active;

    private List<Long> permissions;

}
