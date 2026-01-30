package com.caochung.recruitment.domain.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CompanyResponseDTO {
    private Long id;

    private String name;

    private String description;

    private String address;

    private String logo;
}
