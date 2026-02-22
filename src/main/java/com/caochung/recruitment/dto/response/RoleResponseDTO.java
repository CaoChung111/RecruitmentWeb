package com.caochung.recruitment.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter @Setter
public class RoleResponseDTO {
    private Long id;

    private String name;

    private String description;

    private boolean active;

    private Instant createdAt;

    private List<PermissionRole> permissions;

    @Getter @Setter
    public static class PermissionRole{
        private Long id;
        private String name;
    }
}
