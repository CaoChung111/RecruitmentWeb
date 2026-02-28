package com.caochung.recruitment.dto.response;

import com.caochung.recruitment.util.annotation.Level;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter @Setter
public class JobResponseDTO {
    private Long id;

    private String name;

    private String location;

    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss", timezone = "UTC")
    private Instant startDate;

    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss", timezone = "UTC")
    private Instant endDate;

    private String description;

    private Double salary;

    private Integer quantity;

    private String level;

    private String active;

    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss", timezone = "UTC")
    private Instant createdAt;

    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss", timezone = "UTC")
    private Instant updatedAt;

    private JobCompany company;

    private List<JobSkill> skills;

    @Getter @Setter
    public static class JobSkill{
        private Long id;
        private String name;
    }

    @Getter @Setter
    public static class JobCompany{
        private Long id;
        private String name;
        private String logo;
    }
}
