package com.caochung.recruitment.ai.dto;

import java.util.List;

public record RecommendedJobDTO(
        Long jobId,
        String jobTitle,
        String companyName,
        double matchScore,
        String matchSummary,
        List<String> topStrengths
) {}
