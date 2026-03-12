package com.caochung.recruitment.dto.response;

import com.caochung.recruitment.domain.Skill;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter @Setter
public class SubscriberResponseDTO {
    private Long id;

    private String name;

    private String email;

    private List<SkillResponseDTO> skills;

    private Instant createdAt;

    private Instant updatedAt;
}
