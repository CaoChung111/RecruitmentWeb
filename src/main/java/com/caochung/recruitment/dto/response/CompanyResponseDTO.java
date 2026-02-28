package com.caochung.recruitment.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss", timezone = "UTC")
    private Instant createdAt;

    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss", timezone = "UTC")
    private Instant updatedAt;
}
