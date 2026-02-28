package com.caochung.recruitment.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Getter @Setter
public class RoleResponseDTO {
    private Long id;

    private String name;

    private String description;

    private boolean active;

    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss", timezone = "UTC")
    private Instant createdAt;

    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss", timezone = "UTC")
    private Instant updatedAt;

    private Set<PermissionRole> permissions;

    @Getter @Setter
    public static class PermissionRole{
        private Long id;
        private String name;
        private String apiPath;
        private String method;
        private String module;
    }
}
