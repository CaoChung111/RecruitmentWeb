package com.caochung.recruitment.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter @Setter
public class CompanyResponseDTO {
    private Long id;

    private String name;

    private String description;

    private String address;

    private String logo;

    private Instant createdAt;

    private Instant updatedAt;
}
