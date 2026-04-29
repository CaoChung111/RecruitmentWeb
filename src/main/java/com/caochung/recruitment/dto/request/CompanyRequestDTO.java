package com.caochung.recruitment.dto.request;

import com.caochung.recruitment.constant.CompanyStatusEnum;
import com.caochung.recruitment.util.annotation.CompanyStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import static com.caochung.recruitment.constant.CompanyStatusEnum.*;

@Getter @Setter
@Builder
public class CompanyRequestDTO {
    @NotBlank(message = "Name cannot be empty")
    @Size(min = 2, max = 200, message = "Company name must be between 2 and 200 characters")
    private String name;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @Size(max = 500, message = "Address must not exceed 500 characters")
    private String address;

    @Size(max = 500, message = "Logo URL must not exceed 500 characters")
    private String logo;
}
