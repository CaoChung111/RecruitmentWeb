package com.caochung.recruitment.ai.dto;

import java.util.List;

public record ParsedCvDTO(
        String fullName,
        String email,
        String phone,
        List<String> skills,
        Integer yearsOfExperience,
        String currentPosition,
        String summary,
        String education,
        List<ExperienceDTO> experiences
) {
    public record ExperienceDTO(
      String companyName,
      String position,
      String duration,
      String description
    ) {}
}
