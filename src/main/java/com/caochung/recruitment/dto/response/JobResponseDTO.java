package com.caochung.recruitment.dto.response;

import com.caochung.recruitment.util.annotation.Level;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class JobResponseDTO {
    private Long id;

    private String name;

    private String location;

    private Integer quantity;

    private String level;

    private String active;

    private List<JobSkill> skills;

    @Getter @Setter
    public static class JobSkill{
        private Long id;
        private String name;
    }
}
