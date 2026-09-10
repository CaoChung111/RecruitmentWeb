package com.caochung.recruitment.dto.response;

import com.caochung.recruitment.ai.dto.ParsedCvDTO;
import com.caochung.recruitment.constant.AnalysisStatusEnum;
import lombok.Builder;

import java.util.List;

@Builder
public record ResumeDetailResponseDTO(
        Long id,
        Long resumeId,
        String fullName,
        String email,
        String phone,
        List<String> skills,
        Integer yearsOfExperience,
        String currentPosition,
        String summary,
        String education,
        List<ParsedCvDTO.ExperienceDTO> experiences,
        AnalysisStatusEnum analysisStatus
) {}
