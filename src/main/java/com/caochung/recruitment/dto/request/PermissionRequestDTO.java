package com.caochung.recruitment.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PermissionRequestDTO {
    @NotBlank(message = "Name cannot be empty")
    @Size(min = 2, max = 200, message = "Name must be between 2 and 200 characters")
    private String name;

    @NotBlank(message = "Api path cannot be empty")
    @Size(max = 500, message = "Api path must not exceed 500 characters")
    private String apiPath;

    @NotBlank(message = "Method cannot be empty")
    @Pattern(regexp = "POST|GET|PUT|PATCH|DELETE", message = "Method must be one of: POST, GET, PUT, PATCH, DELETE")
    private String method;

    @NotBlank(message = "Module cannot be empty")
    @Size(min = 2, max = 100, message = "Module name must be between 2 and 100 characters")
    private String module;
}
