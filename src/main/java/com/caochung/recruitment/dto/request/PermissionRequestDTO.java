package com.caochung.recruitment.dto.request;

import com.caochung.recruitment.domain.Role;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToMany;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class PermissionRequestDTO {
    @NotBlank(message = "Name cannot be empty")
    private String name;

    @NotBlank(message = "Api path cannot be empty")
    private String apiPath;

    @NotBlank(message = "Method cannot be empty")
    @Pattern(regexp = "POST|GET|PUT|PATCH|DELETE", message = "Method invalid")
    private String method;

    @NotBlank(message = "Module cannot be empty")
    private String module;
}
