package com.caochung.recruitment.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class UserResponseDTO {
    private Long id;
    private  String name;
    private String email;
    private int age;
    private String gender;
    private String address;
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss", timezone = "UTC")
    private Instant createdAt;
    private CompanyResponseDTO company;

    @Getter @Setter
    public static class CompanyResponseDTO {
        private Long id;
        private String name;
    }
}
