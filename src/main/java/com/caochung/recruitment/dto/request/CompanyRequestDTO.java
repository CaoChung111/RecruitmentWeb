package com.caochung.recruitment.dto.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Builder
public class CompanyRequestDTO {
    @NotBlank(message = "Name cannot be empty")
    private String name;

    private String description;

    private String address;

    private String logo;
}
