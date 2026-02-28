package com.caochung.recruitment.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponseDTO {
    private Long id;
    private  String name;
    private String email;
    private int age;
    private String gender;
    private String address;
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss", timezone = "UTC")
    private Instant createdAt;
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss", timezone = "UTC")
    private Instant updatedAt;
    private CompanyResponseDTO company;
    private RoleResponseDTO role;

    @Getter @Setter
    public static class CompanyResponseDTO {
        private Long id;
        private String name;
    }

    @Getter @Setter
    public static class RoleResponseDTO {
        private Long id;
        private String name;
    }
}
