package com.caochung.recruitment.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter @Setter
public class PermissionResponseDTO {
    private Long id;

    private String name;

    private String apiPath;

    private String method;

    private String module;

    private Instant createdAt;
}
