package com.caochung.recruitment.dto.request;

import com.caochung.recruitment.constant.CompanyStatusEnum;
import com.caochung.recruitment.util.annotation.CompanyStatus;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import static com.caochung.recruitment.constant.CompanyStatusEnum.*;

@Getter @Setter
@Builder
public class CompanyRequestDTO {
    @NotBlank(message = "Name cannot be empty")
    private String name;

    private String description;

    private String address;

    private String logo;
}
