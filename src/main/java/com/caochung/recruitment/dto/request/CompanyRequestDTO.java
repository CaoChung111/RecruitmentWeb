package com.caochung.recruitment.dto.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Builder
public class CompanyRequestDTO {
    @NotBlank(message = "Tên công ty không được để trống")
    private String name;

    private String description;

    private String address;

    private String logo;
}
